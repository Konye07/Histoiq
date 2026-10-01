import { execFileSync } from 'node:child_process';
import { createPrivateKey, randomBytes, sign } from 'node:crypto';
import { copyFileSync, mkdirSync, mkdtempSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, resolve, sep } from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseArgs } from 'node:util';

const { values: args } = parseArgs({
  options: {
    key: { type: 'string' },
    'key-id': { type: 'string' },
    issuer: { type: 'string' },
    out: { type: 'string' },
    'bundle-id': { type: 'string', default: 'com.konye.historiq' },
    'profile-name': { type: 'string', default: 'Historiq App Store' },
  },
});

for (const required of ['key', 'key-id', 'issuer', 'out']) {
  if (!args[required]) {
    console.error('Usage: node scripts/ios-signing.mjs --key <AuthKey_XXXX.p8> --key-id <id> --issuer <uuid> --out <dir outside the repo>');
    process.exit(1);
  }
}

const repoRoot = resolve(fileURLToPath(new URL('..', import.meta.url)));
const outDir = resolve(args.out);
if (outDir === repoRoot || outDir.startsWith(repoRoot + sep)) {
  throw new Error('--out must be outside the repository: the bundle contains private keys');
}

const keyPem = readFileSync(args.key, 'utf8');
const b64url = (data) => Buffer.from(data).toString('base64url');

function jwt() {
  const now = Math.floor(Date.now() / 1000);
  const input = [
    { alg: 'ES256', kid: args['key-id'], typ: 'JWT' },
    { iss: args.issuer, iat: now, exp: now + 600, aud: 'appstoreconnect-v1' },
  ].map((part) => b64url(JSON.stringify(part))).join('.');
  const signature = sign('sha256', Buffer.from(input), { key: createPrivateKey(keyPem), dsaEncoding: 'ieee-p1363' });
  return `${input}.${b64url(signature)}`;
}

async function api(method, path, body) {
  const response = await fetch(`https://api.appstoreconnect.apple.com${path}`, {
    method,
    headers: { Authorization: `Bearer ${jwt()}`, 'Content-Type': 'application/json' },
    body: body && JSON.stringify(body),
  });
  const json = response.status === 204 ? {} : await response.json().catch(() => ({}));
  if (!response.ok) {
    const detail = (json.errors ?? []).map((e) => `${e.title}: ${e.detail}`).join('; ');
    throw new Error(`${method} ${path} -> HTTP ${response.status} ${detail}`);
  }
  return json;
}

function openssl(opts, ...opensslArgs) {
  return execFileSync('openssl', opensslArgs, {
    env: { ...process.env, MSYS_NO_PATHCONV: '1', ...opts?.env },
    stdio: ['ignore', 'pipe', 'pipe'],
    cwd: opts?.cwd,
  });
}

const bundles = await api('GET', `/v1/bundleIds?filter[identifier]=${encodeURIComponent(args['bundle-id'])}&limit=50`);
const bundle = bundles.data.find((b) => b.attributes.identifier === args['bundle-id']);
if (!bundle) throw new Error(`Bundle ID ${args['bundle-id']} is not registered in the developer account`);

const capabilities = await api('GET', `/v1/bundleIds/${bundle.id}/bundleIdCapabilities`);
if (!capabilities.data.some((c) => c.attributes.capabilityType === 'APPLE_ID_AUTH')) {
  throw new Error('Sign in with Apple is not enabled on the bundle ID: enable it in the developer portal first, or the profile will lack the entitlement');
}

const staleProfiles = await api('GET', `/v1/profiles?filter[name]=${encodeURIComponent(args['profile-name'])}&limit=50`);
for (const profile of staleProfiles.data.filter((p) => p.attributes.name === args['profile-name'])) {
  await api('DELETE', `/v1/profiles/${profile.id}`);
  console.log(`Deleted stale profile ${profile.id}`);
}

const work = mkdtempSync(join(tmpdir(), 'ios-signing-'));
try {
  const keyFile = join(work, 'dist.key');
  const csrFile = join(work, 'dist.csr');
  openssl({}, 'req', '-new', '-newkey', 'rsa:2048', '-nodes', '-keyout', keyFile, '-out', csrFile, '-subj', '/CN=Historiq Distribution/O=Historiq');

  const certificate = (await api('POST', '/v1/certificates', {
    data: { type: 'certificates', attributes: { certificateType: 'DISTRIBUTION', csrContent: readFileSync(csrFile, 'utf8') } },
  })).data;
  const derFile = join(work, 'dist.cer');
  const pemFile = join(work, 'dist.pem');
  writeFileSync(derFile, Buffer.from(certificate.attributes.certificateContent, 'base64'));
  openssl({}, 'x509', '-inform', 'der', '-in', derFile, '-out', pemFile);

  // 3DES/SHA1 rather than OpenSSL 3's defaults: macOS `security import` rejects the newer PKCS#12 MAC with "MAC verification failed".
  const password = randomBytes(24).toString('base64url');
  const stage = join(work, 'bundle');
  mkdirSync(stage);
  openssl(
    { env: { P12_PASSWORD: password } },
    'pkcs12', '-export', '-inkey', keyFile, '-in', pemFile, '-out', join(stage, 'dist.p12'),
    '-passout', 'env:P12_PASSWORD', '-certpbe', 'PBE-SHA1-3DES', '-keypbe', 'PBE-SHA1-3DES', '-macalg', 'sha1',
  );

  const profile = (await api('POST', '/v1/profiles', {
    data: {
      type: 'profiles',
      attributes: { name: args['profile-name'], profileType: 'IOS_APP_STORE' },
      relationships: {
        bundleId: { data: { type: 'bundleIds', id: bundle.id } },
        certificates: { data: [{ type: 'certificates', id: certificate.id }] },
      },
    },
  })).data;

  writeFileSync(join(stage, 'p12-password.txt'), password);
  writeFileSync(join(stage, 'profile.mobileprovision'), Buffer.from(profile.attributes.profileContent, 'base64'));
  copyFileSync(args.key, join(stage, 'AuthKey.p8'));

  // Relative paths on purpose: GNU tar (Git for Windows) reads "D:\..." as a remote host:path.
  execFileSync('tar', ['-czf', '../bundle.tgz', '.'], { cwd: stage });

  mkdirSync(outDir, { recursive: true });
  const bundleFile = join(outDir, 'IOS_SIGNING_BUNDLE.txt');
  writeFileSync(bundleFile, readFileSync(join(work, 'bundle.tgz')).toString('base64'));

  console.log(`Certificate ${certificate.id}: ${certificate.attributes.name}, expires ${certificate.attributes.expirationDate}`);
  console.log(`Profile ${profile.attributes.uuid}: ${profile.attributes.name}, expires ${profile.attributes.expirationDate}`);
  console.log(`Wrote ${bundleFile}`);
} finally {
  rmSync(work, { recursive: true, force: true });
}

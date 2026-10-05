-- Account deletion removes the sign-in record too.
--
-- The app's Delete Account (AccountRepository.deleteAccount) deletes the user's rows and, last, their
-- profiles row, then signs out. It cannot delete the auth.users row itself: that needs the service role,
-- which a client must never hold. Deleting the profile therefore also deletes the auth user, from the
-- database side, so every shipped app version gets the complete deletion with no update. The auth user's
-- own ON DELETE CASCADE removes whatever the client did not already delete (score_history, quiz_progress)
-- and Supabase removes its identities, sessions and refresh tokens, including the email and provider id.
--
-- pg_trigger_depth() > 1 skips profiles deleted by that same cascade (auth.users -> profiles), which are
-- already on their way out. device_audit_log rows stay, detached from the account by their existing
-- ON DELETE SET NULL, as the security audit log is documented to be. Safe to run more than once.

create or replace function public.delete_auth_user_with_profile()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  if pg_trigger_depth() > 1 then
    return old;
  end if;
  delete from public.friend_code_lookup_attempts where caller_id = old.id;
  delete from auth.users where id = old.id;
  return old;
end;
$$;

revoke all on function public.delete_auth_user_with_profile() from public, anon, authenticated;

drop trigger if exists delete_auth_user_after_profile_delete on public.profiles;
create trigger delete_auth_user_after_profile_delete
  after delete on public.profiles
  for each row execute function public.delete_auth_user_with_profile();

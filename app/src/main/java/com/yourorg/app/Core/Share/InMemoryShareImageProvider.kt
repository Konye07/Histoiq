package com.yourorg.app.Core.Share

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import java.io.FileNotFoundException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

private const val EXPIRY_MS = 5 * 60_000L

/**
 * Serves a shared result image entirely from process memory — no cache file, no temp file, ever.
 * `put()` stashes PNG bytes under a random id and hands back a content Uri; `openFile()` pipes
 * those bytes straight to whoever asks. Entries are NOT deleted on first read: the system share
 * sheet itself opens the Uri once to render its own preview thumbnail before the user even picks
 * a target app, so a delete-on-read design would make the real share (to Messages/Gmail/etc.)
 * fail right after the sheet's own preview succeeded — confirmed live, this actually happened.
 * Bytes are only ever dropped by the timed expiry below, so nothing lingers indefinitely, but the
 * image stays readable for the whole chooser interaction.
 */
class InMemoryShareImageProvider : ContentProvider() {

    companion object {
        private val pending = ConcurrentHashMap<String, ByteArray>()

        fun put(context: Context, bytes: ByteArray): Uri {
            val id = UUID.randomUUID().toString()
            pending[id] = bytes
            Handler(Looper.getMainLooper()).postDelayed({ pending.remove(id) }, EXPIRY_MS)
            return Uri.parse("content://${context.packageName}.shareimage/share/$id.png")
        }
    }

    override fun onCreate(): Boolean = true

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        val id = uri.lastPathSegment?.removeSuffix(".png")
            ?: throw FileNotFoundException("Malformed share image uri: $uri")
        val bytes = pending[id]
            ?: throw FileNotFoundException("Share image expired: $uri")

        val (readSide, writeSide) = ParcelFileDescriptor.createPipe()
        thread {
            ParcelFileDescriptor.AutoCloseOutputStream(writeSide).use { it.write(bytes) }
        }
        return readSide
    }

    override fun getType(uri: Uri): String = "image/png"

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
}

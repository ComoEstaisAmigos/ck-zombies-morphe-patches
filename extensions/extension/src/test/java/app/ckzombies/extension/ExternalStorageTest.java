package app.ckzombies.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExternalStorageTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    /** Says a file cannot be opened when its name starts with "locked". */
    private static final ExternalStorage.Probe BY_NAME = new ExternalStorage.Probe() {
        public boolean opens(File file) {
            return !file.getName().startsWith("locked");
        }
    };

    private File sound(File folder, String name) throws IOException {
        File file = new File(folder, name);
        FileOutputStream out = new FileOutputStream(file);
        out.write(new byte[] {'O', 'g', 'g', 'S'});
        out.close();
        return file;
    }

    @Test
    public void theSoundCacheFolderIsMadeAndWhatIsInItStays() throws IOException {
        File files = temp.newFolder("files");

        File folder = ExternalStorage.soundCache(files);
        assertEquals(new File(new File(files, ".cache"), ".media"), folder);
        assertTrue(folder.isDirectory());

        File music = sound(folder, "IDM_MENU_MUSIC");
        assertEquals(folder, ExternalStorage.soundCache(files));
        assertTrue(music.exists());
    }

    @Test
    public void onlyTheFilesThatDoNotOpenAreDeleted() throws IOException {
        File folder = temp.newFolder(".media");
        File fire = sound(folder, "locked_IDM_FIRE_SHOTGUN");
        File ambient = sound(folder, "locked_IDM_AMBIENT_1");
        File reload = sound(folder, "IDM_RELOAD_SHOTGUN_1");

        assertEquals(2, ExternalStorage.removeUnreadable(folder, BY_NAME));
        assertFalse(fire.exists());
        assertFalse(ambient.exists());
        assertTrue(reload.exists());
        assertEquals("a second pass finds nothing left", 0, ExternalStorage.removeUnreadable(folder, BY_NAME));
    }

    @Test
    public void aFolderIsNeverDeleted() throws IOException {
        File folder = temp.newFolder(".media");
        File inner = new File(folder, "locked_folder");
        assertTrue(inner.mkdir());

        assertEquals(0, ExternalStorage.removeUnreadable(folder, BY_NAME));
        assertTrue(inner.isDirectory());
    }

    @Test
    public void aMissingFolderIsFine() {
        assertEquals(0, ExternalStorage.removeUnreadable(new File(temp.getRoot(), "nowhere"), BY_NAME));
    }

    @Test
    public void theRealProbeOpensAReadableFile() throws IOException {
        File folder = temp.newFolder(".media");
        File file = sound(folder, "IDM_HEADSHOT");

        assertTrue(ExternalStorage.OPEN_AND_CLOSE.opens(file));
        assertEquals(0, ExternalStorage.removeUnreadable(folder, ExternalStorage.OPEN_AND_CLOSE));
        assertTrue(file.exists());
    }

    @Test
    public void theRealProbeRejectsAFileWithoutReadPermission() throws IOException {
        File folder = temp.newFolder(".media");
        File file = sound(folder, "IDM_FIRE_SHOTGUN");
        // Windows cannot take read permission away, and root reads everything; skipped there.
        assumeTrue(file.setReadable(false, false) && !file.canRead());

        assertFalse(ExternalStorage.OPEN_AND_CLOSE.opens(file));
        assertEquals(1, ExternalStorage.removeUnreadable(folder, ExternalStorage.OPEN_AND_CLOSE));
        assertFalse(file.exists());
    }
}

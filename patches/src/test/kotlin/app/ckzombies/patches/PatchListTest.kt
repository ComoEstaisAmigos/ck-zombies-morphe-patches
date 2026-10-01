package app.ckzombies.patches

import app.ckzombies.patches.compat.deadServersPatch
import app.ckzombies.patches.compat.modernAndroidPatch
import app.ckzombies.patches.compat.obbMessagePatch
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** What a player sees in the patch list. */
class PatchListTest {
    @Test
    fun `the missing OBB message comes with either patch and is described by Modern Android compatibility`() {
        assertTrue(obbMessagePatch in modernAndroidPatch.dependencies)
        assertTrue(obbMessagePatch in deadServersPatch.dependencies)
        assertTrue("If the OBB is missing" in modernAndroidPatch.description!!)
        assertFalse("OBB" in deadServersPatch.description!!)
    }
}

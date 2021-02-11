package cz.tomashavlicek.mildybackgroundlocation.permissions

interface PermissionCallback {

    /**
     * All requested permissions were granted.
     */
    fun onPermissionsGranted(requestCode: Int)

    /**
     * At least one permission was denied and none of these were denied forever.
     */
    fun onPermissionsDenied(requestCode: Int)

    /**
     * At least one permission was denied forever.
     */
    fun onPermissionsDeniedForever(requestCode: Int)
}
package com.rushkar.pingzz.extensions

import android.content.Context
import android.location.LocationManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.rushkar.pingzz.utils.NetworkUtils

fun AppCompatActivity.setToolbarWithBack(toolbar: Toolbar, resId: Int) {
    setToolbarWithBack(toolbar, getString(resId))
}

fun AppCompatActivity.setToolbarWithBack(toolbar: Toolbar, title: String?) {
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayShowHomeEnabled(true)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.title = ""
//    toolbar.findViewById<TextView>(R.id.toolbarTitle).text = title
}

fun AppCompatActivity.setToolbar(toolbar: Toolbar, resId: Int) {
    setToolbar(toolbar, getString(resId))
}

fun AppCompatActivity.setToolbar(toolbar: Toolbar, title: String?) {
    setSupportActionBar(toolbar)
    supportActionBar?.title = ""
//    toolbar.findViewById<TextView>(R.id.toolbarTitle).text = title
}

fun AppCompatActivity.showToast(message: String?) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.isLocationEnabled(): Boolean {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

fun AppCompatActivity.isInternetNotAvailable(): Boolean {
    return !NetworkUtils.isInternetAvailable(this)
}

/*fun AppCompatActivity.replaceFragments(
    fragment: Fragment?,
    addToBackStack: Boolean,
    tag: String? = fragment?.tag,
    container: Int = R.id.container
) {
    if (fragment == null) return
    val manager = supportFragmentManager
    val ft: FragmentTransaction = manager.beginTransaction()
    if (addToBackStack) {
        ft.replace(container, fragment, tag)
        ft.addToBackStack(tag)
        ft.commitAllowingStateLoss()
    } else {
        manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        ft.replace(container, fragment, tag)
        ft.commit()
    }
}*/

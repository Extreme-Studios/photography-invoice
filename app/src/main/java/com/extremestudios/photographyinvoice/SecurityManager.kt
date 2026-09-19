package com.extremestudios.photographyinvoicelite

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.util.Base64
import android.util.Log
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Security Manager for Photography Invoice
 *
 * Handles access validation and app signature verification.
 */
object SecurityManager {
    
    private const val TAG = "SecurityManager"
    
    // Expected app signature for release builds (SHA-256)
    // This should be updated with your actual release signature
    private val EXPECTED_SIGNATURES = emptySet<String>()
    
    fun isFullBuild(): Boolean {
        return true
    }
    
    fun isPremiumAccessAuthorized(sharedPref: SharedPreferences): Boolean {
        return true
    }
    
    fun resetFakePremiumData(context: Context) {
        return
        
        try {
            val sharedPref = context.getSharedPreferences("invoice_prefs", Context.MODE_PRIVATE)
            
        } catch (_: Exception) {
        }
    }
    
    /**
     * Validate app signature to prevent tampering
     */
    fun isAppSignatureValid(): Boolean {
        // For development/debug builds, skip signature check
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Debug build detected, skipping signature check")
            return true
        }
        
        // If no expected signatures are set, allow for now
        // (You should add your actual release signatures)
        if (EXPECTED_SIGNATURES.isEmpty()) {
            Log.w(TAG, "No expected signatures configured, allowing for now")
            return true
        }
        
        return try {
            val packageManager = App.getContext().packageManager
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(
                    App.getContext().packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(
                    App.getContext().packageName,
                    PackageManager.GET_SIGNATURES
                )
            }
            
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }
            
            signatures?.any { signature ->
                val signatureHash = getSignatureHash(signature)
                EXPECTED_SIGNATURES.contains(signatureHash)
            } ?: false
            
        } catch (e: Exception) {
            Log.e(TAG, "Error validating app signature: ${e.message}")
            false
        }
    }
    
    /**
     * Get SHA-256 hash of app signature
     */
    private fun getSignatureHash(signature: Signature): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(signature.toByteArray())
        val digest = md.digest()
        
        return digest.joinToString(":") { 
            String.format("%02X", it) 
        }
    }
    
    fun isPlayBillingEnabled(): Boolean {
        return false
    }
    
    /**
     * Log security events for monitoring
     */
    fun logSecurityEvent(event: String, details: String = "") {
        Log.i(TAG, "SECURITY_EVENT: $event${if (details.isNotEmpty()) " - $details" else ""}")
        
        // In production, you might want to send this to a remote logging service
        // to detect tampering attempts
    }
    
    /**
     * Check for suspicious activity patterns
     */
    fun detectSuspiciousActivity(context: Context): Boolean {
        return false
    }
    
    /**
     * Increment premium change counter for anomaly detection
     */
    fun trackPremiumStatusChange(context: Context) {
        val sharedPref = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
        val count = sharedPref.getInt("premium_change_count", 0)
        sharedPref.edit().putInt("premium_change_count", count + 1).apply()
    }
}


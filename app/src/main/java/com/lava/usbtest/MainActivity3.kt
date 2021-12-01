
package com.lava.usbtest;
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.*
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.lava.usbtest.R


class MainActivity3 : AppCompatActivity() {
    private var mUsbManager: UsbManager? = null
    private var mDevice: UsbDevice? = null
    private var mConnection: UsbDeviceConnection? = null
    private var mInterface: UsbInterface? = null
    private var mEndPoint: UsbEndpoint? = null
    private var mPermissionIntent: PendingIntent? = null
    var ed_txt: EditText? = null
    var mDeviceList: HashMap<String, UsbDevice>? = null
    var mDeviceIterator: Iterator<UsbDevice>? = null
    lateinit var testBytes: ByteArray

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main2)

        ed_txt = findViewById<View>(R.id.ed_txt) as EditText
        val print: Button = findViewById<View>(R.id.print) as Button
        mUsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        mDeviceList = mUsbManager!!.deviceList
        if ((mDeviceList?.size)!! > 0) {
            mDeviceIterator = mDeviceList?.values?.iterator()
            Toast.makeText(
                this,
                "Device List Size: " + java.lang.String.valueOf(mDeviceList?.size),
                Toast.LENGTH_SHORT
            ).show()
            val textView = findViewById<View>(R.id.usbDevice) as TextView
            var usbDevice = ""
            while (mDeviceIterator!!.hasNext()) {
                val usbDevice1 = mDeviceIterator!!.next()
                usbDevice += """
DeviceID: ${usbDevice1.deviceId}
DeviceName: ${usbDevice1.deviceName}
Protocol: ${usbDevice1.deviceProtocol}
Product Name: ${usbDevice1.productName}
Manufacturer Name: ${usbDevice1.manufacturerName}
DeviceClass: ${usbDevice1.deviceClass} - ${translateDeviceClass(usbDevice1.deviceClass)}
DeviceSubClass: ${usbDevice1.deviceSubclass}
VendorID: ${usbDevice1.vendorId}
ProductID: ${usbDevice1.productId}
"""
                val interfaceCount = usbDevice1.interfaceCount
                Toast.makeText(this, "INTERFACE COUNT: $interfaceCount", Toast.LENGTH_SHORT).show()
                mDevice = usbDevice1
                Toast.makeText(this, "Device is attached", Toast.LENGTH_SHORT).show()
                textView.text = usbDevice
            }
            mPermissionIntent =
                PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
            val filter = IntentFilter(ACTION_USB_PERMISSION)
            registerReceiver(mUsbReceiver, filter)
            mUsbManager!!.requestPermission(mDevice, mPermissionIntent)
        } else {
            Toast.makeText(this, "Please attach printer via USB", Toast.LENGTH_SHORT).show()
        }
        print.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                print(mConnection, mInterface)
            }


        })
    }

    private fun print(connection: UsbDeviceConnection?, usbInterface: UsbInterface?) {
        val test = """
            ${ed_txt!!.text}
            
            
            """.trimIndent()
        testBytes = test.toByteArray()
        if (usbInterface == null) {
            Toast.makeText(this, "INTERFACE IS NULL", Toast.LENGTH_SHORT).show()
        } else if (connection == null) {
            Toast.makeText(this, "CONNECTION IS NULL", Toast.LENGTH_SHORT).show()
        } else if (forceCLaim == null) {
            Toast.makeText(this, "FORCE CLAIM IS NULL", Toast.LENGTH_SHORT).show()
        } else {
            connection.claimInterface(usbInterface, forceCLaim)
            val thread = Thread {
                val cut_paper = byteArrayOf(0x1D, 0x56, 0x41, 0x10)
                connection.bulkTransfer(mEndPoint, testBytes, testBytes.size, 0)
                connection.bulkTransfer(mEndPoint, cut_paper, cut_paper.size, 0)
            }
            thread.run()
        }
    }

    val mUsbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    val device =
                        intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            //call method to set up device communication
                            mInterface = device.getInterface(0)
                            mEndPoint = mInterface!!.getEndpoint(1) // 0 IN and  1 OUT to printer.
                            mConnection = mUsbManager!!.openDevice(device)
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "PERMISSION DENIED FOR THIS DEVICE",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun translateDeviceClass(deviceClass: Int): String {
        return when (deviceClass) {
            UsbConstants.USB_CLASS_APP_SPEC -> "Application specific USB class"
            UsbConstants.USB_CLASS_AUDIO -> "USB class for audio devices"
            UsbConstants.USB_CLASS_CDC_DATA -> "USB class for CDC devices (communications device class)"
            UsbConstants.USB_CLASS_COMM -> "USB class for communication devices"
            UsbConstants.USB_CLASS_CONTENT_SEC -> "USB class for content security devices"
            UsbConstants.USB_CLASS_CSCID -> "USB class for content smart card devices"
            UsbConstants.USB_CLASS_HID -> "USB class for human interface devices (for example, mice and keyboards)"
            UsbConstants.USB_CLASS_HUB -> "USB class for USB hubs"
            UsbConstants.USB_CLASS_MASS_STORAGE -> "USB class for mass storage devices"
            UsbConstants.USB_CLASS_MISC -> "USB class for wireless miscellaneous devices"
            UsbConstants.USB_CLASS_PER_INTERFACE -> "USB class indicating that the class is determined on a per-interface basis"
            UsbConstants.USB_CLASS_PHYSICA -> "USB class for physical devices"
            UsbConstants.USB_CLASS_PRINTER -> "USB class for printers"
            UsbConstants.USB_CLASS_STILL_IMAGE -> "USB class for still image devices (digital cameras)"
            UsbConstants.USB_CLASS_VENDOR_SPEC -> "Vendor specific USB class"
            UsbConstants.USB_CLASS_VIDEO -> "USB class for video devices"
            UsbConstants.USB_CLASS_WIRELESS_CONTROLLER -> "USB class for wireless controller devices"
            else -> "Unknown USB class!"
        }
    }

    companion object {
        private const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
        private val forceCLaim: Boolean? = true
    }
}

private fun Button.setOnClickListener(any: Any) {


}

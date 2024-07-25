package com.example.waterlock.presentation

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterlock.R
import com.example.waterlock.presentation.theme.Grey1
import com.example.waterlock.presentation.theme.Grey2
import com.example.waterlock.presentation.theme.MyGreen
import com.example.waterlock.presentation.theme.WaterLockTheme
import com.samsung.android.sdk.accessory.SAAgentV2
import com.samsung.android.sdk.accessory.SAAgentV2.RequestAgentCallback
import kotlin.math.roundToInt
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import com.samsung.android.sdk.SsdkUnsupportedException
import com.samsung.android.sdk.accessory.SA

class MainActivity : ComponentActivity() {

    private lateinit var providerService: MyProviderService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")

        if (!isSamsungAccessoryServiceInstalled()) {
            Log.e("MainActivity", "Samsung Accessory Service not found")
            showInstallSamsungAccessoryServiceDialog()
            return
        }

        // Инициализация Samsung Accessory SDK
        try {
            val accessory = SA()
            accessory.initialize(this)
        } catch (e: SsdkUnsupportedException) {
            Log.e("MainActivity", "Samsung Accessory SDK не поддерживается", e)
        } catch (e: Exception) {
            Log.e("MainActivity", "Ошибка инициализации Samsung Accessory SDK", e)
        }

        // Используйте requestAgent для инициализации MyProviderService
        SAAgentV2.requestAgent(this, MyProviderService::class.java.name, object : RequestAgentCallback {
            override fun onAgentAvailable(agent: SAAgentV2?) {
                Log.d("MainActivity", "Agent available")
                providerService = agent as MyProviderService
                setContent {
                    WaterLockTheme {
                        WaterLockScreen { enableWaterLock() }
                    }
                }
            }

            override fun onError(error: Int, message: String?) {
                // Обработка ошибки инициализации
                Log.e("MainActivity", "Ошибка инициализации: $error, $message")
                showToast("Ошибка инициализации: $error, $message")
            }
        })
    }

    private fun isSamsungAccessoryServiceInstalled(): Boolean {
        return try {
            packageManager.getPackageInfo("com.samsung.android.sdk.accessory", PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("MainActivity", "Samsung Accessory Service not found")
            false
        }
    }

    private fun showInstallSamsungAccessoryServiceDialog() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=com.samsung.android.sdk.accessory")
            setPackage("com.android.vending")
        }
        startActivity(intent)
    }

    private fun enableWaterLock() {
        try {
            Log.d("MainActivity", "Enabling Water Lock")
            providerService.sendWaterLockCommand()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error enabling Water Lock", e)
            e.printStackTrace()
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
@Composable
fun WaterLockScreen(onWaterLockEnabled: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_location),
                contentDescription = "Location",
                tint = MyGreen
            )
            Text(
                text = "13:25",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        AvgDurationAndDistance()
        Spacer(modifier = Modifier.height(8.dp))
        AvgPaceAndCalories()
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "--",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
    WaterLockIcon(onWaterLockEnabled)
}

@Composable
fun AvgDurationAndDistance() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly) {
            Text(
                text = "Duration",
                fontSize = 12.sp,
                color = Grey1
            )
            Text(
                text = "Distance",
                fontSize = 12.sp,
                color = Grey1
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Grey2, shape = RoundedCornerShape(32.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "00:00",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .padding(top = 8.dp, bottom = 8.dp)
                        .background(Grey1)
                )
                Text(
                    text = "0.00",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun AvgPaceAndCalories() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly) {
            Text(
                text = "Avg. pace",
                fontSize = 12.sp,
                color = Grey1
            )
            Text(
                text = "Calories",
                fontSize = 12.sp,
                color = Grey1
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Grey2, shape = RoundedCornerShape(32.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "--'--\"",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .padding(top = 8.dp, bottom = 8.dp)
                        .background(Grey1)
                )
                Text(
                    text = "  0  ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun WaterLockIcon(onWaterLockEnabled: () -> Unit) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size(24.dp, 24.dp)
                .background(Color.Cyan, RoundedCornerShape(32.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
                .clickable {
                    Log.d("WaterLock", "Icon Clicked")
                    onWaterLockEnabled()
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_waterlock),
                contentDescription = "Water Lock Icon",
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 320, heightDp = 320)
@Composable
fun DefaultPreview() {
    WaterLockTheme {
        WaterLockScreen {}
    }
}
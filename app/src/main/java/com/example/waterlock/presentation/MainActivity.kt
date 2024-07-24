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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import com.example.waterlock.R
import com.example.waterlock.presentation.theme.Grey1
import com.example.waterlock.presentation.theme.Grey2
import com.example.waterlock.presentation.theme.MyGreen
import com.example.waterlock.presentation.theme.WaterLockTheme
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WaterLockTheme {
                WaterLockScreen { enableWaterLockMode() }
            }
        }
    }

    private fun enableWaterLockMode() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("WaterLock", "Attempting to get connected nodes")
                val nodeClient = Wearable.getNodeClient(this@MainActivity)
                val nodes = Tasks.await(nodeClient.connectedNodes)
                if (nodes.isNotEmpty()) {
                    for (node in nodes) {
                        Log.d("WaterLock", "Found connected node: ${node.id}")
                        val messageClient = Wearable.getMessageClient(this@MainActivity)
                        val result = Tasks.await(
                            messageClient.sendMessage(
                                node.id,
                                "/water_lock_mode",
                                byteArrayOf()
                            )
                        )
                        if (result == 0) {
                            Log.d("WaterLock", "Water Lock Enabled")
                        } else {
                            Log.e("WaterLock", "Failed to enable Water Lock")
                        }
                    }
                } else {
                    Log.e("WaterLock", "No connected nodes found")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("WaterLock", "Error enabling Water Lock: ${e.message}")
            }
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
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

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
// GeoSurvey-Toolbox/app/src/main/java/com/geosurvey/ui/screens/MainScreen.kt
package com.geosurvey.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.geosurvey.ui.components.GlassCard
import com.geosurvey.ui.theme.*
import com.geosurvey.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val gnssData by viewModel.gnssData.collectAsState()
    val isTracking by viewModel.isTracking.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "GeoSurvey 地质勘查工具箱",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isTracking) viewModel.stopTracking() else viewModel.startTracking("野外勘查轨迹")
                },
                containerColor = if (isTracking) StatusPoor else StatusGood,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = if (isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isTracking) "停止记录" else "开始记录",
                    tint = TextPrimary
                )
            }
        },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 定位状态卡片
            GlassCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "GNSS定位状态",
                        style = MaterialTheme.typography.titleLarge,
                        color = AccentCyan
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    gnssData?.let { data ->
                        DataRow("纬度", String.format("%.6f°", data.latitude))
                        DataRow("经度", String.format("%.6f°", data.longitude))
                        DataRow("海拔", String.format("%.1f m", data.altitude))
                        DataRow("精度", String.format("%.1f m", data.accuracy))
                        DataRow("速度", String.format("%.1f m/s", data.speed))
                        DataRow("方向", String.format("%.1f°", data.bearing))
                        DataRow("卫星数", "${data.satelliteCount} 颗")
                        DataRow("HDOP", String.format("%.1f", data.hdop))
                        DataRow("PDOP", String.format("%.1f", data.pdop))
                        DataRow("UTC时间", data.utcTime)

                        Spacer(modifier = Modifier.height(8.dp))
                        QualityBadge(data.qualityScore)
                    } ?: Text(
                        "正在搜索卫星信号...",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // 卫星信息卡片
            GlassCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "卫星星座分布",
                        style = MaterialTheme.typography.titleLarge,
                        color = AccentCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val satellites by viewModel.satellites.collectAsState()
                    if (satellites.isEmpty()) {
                        Text("暂无卫星数据", color = TextMuted)
                    } else {
                        val grouped = satellites.groupBy { it.constellation }
                        grouped.forEach { (constellation, sats) ->
                            val used = sats.count { it.usedInFix }
                            Text(
                                "$constellation: ${sats.size}颗 (定位使用 $used 颗)",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            // 功能入口
            GlassCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "功能模块",
                        style = MaterialTheme.typography.titleLarge,
                        color = AccentCyan
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("• 轨迹记录与管理", color = TextSecondary)
                    Text("• 产状测量", color = TextSecondary)
                    Text("• 坐标转换", color = TextSecondary)
                    Text("• 地图浏览", color = TextSecondary)
                    Text("• 水印相机", color = TextSecondary)
                    Text("• 地质分析", color = TextSecondary)
                    Text("• 钻孔计算", color = TextSecondary)
                    Text("• 轨迹导航", color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun DataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, style = MaterialTheme.typography.bodyLarge)
        Text(value, color = TextPrimary, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun QualityBadge(score: Int) {
    val (color, text) = when {
        score >= 90 -> StatusGood to "定位质量: 优秀"
        score >= 70 -> StatusFair to "定位质量: 良好"
        score >= 50 -> Color(0xFFFF9800) to "定位质量: 一般"
        score > 0 -> StatusPoor to "定位质量: 较差"
        else -> StatusInvalid to "定位无效"
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = color,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

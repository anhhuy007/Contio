package com.example.contio.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSimple
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.contio.R
import com.example.contio.ui.data.UserDetailsState
import com.example.contio.ui.theme.ContioTheme
import com.example.contio.viewmodels.UserDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.toResult
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class UserDetailsActivity : ComponentActivity() {

    private val viewModel : UserDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = intent.getStringExtra("userId")

        Log.d("AnhHuy", "User ID: $userId")

        setContent{
            ChatTheme {
                UserDetailsScreen(viewModel.userDetailUiState.collectAsState().value)
            }
        }
    }

    @Composable
    private fun UserDetailsScreen(userDetailsUiState: UserDetailsState) {
        viewModel.getCurrentUser()
        Log.d("AnhHuy", "User Name: ${userDetailsUiState.user.name}")

        Scaffold(
            topBar = {
                UserDetailsTopAppBar(onBackButtonClicked = { onBackPressed() }, user = userDetailsUiState.user)
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
                    .verticalScroll(rememberScrollState())
            ) {
                UserAvatarAndOverall(currentUser = userDetailsUiState.user)
            }
        }
    }

    @Composable
    private fun UserDetailsTopAppBar(
        onBackButtonClicked: () -> Unit,
        user: User,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackButtonClicked,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null
                )
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 40.dp)
            ) {
                Text(
                    text = "My profile",
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }

    @Composable
    private fun UserAvatarAndOverall(currentUser: User) {
        if (currentUser != User(id = "local", name = "local")) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.Center),
                    verticalArrangement = Arrangement.Center
                ) {
//                    UserAvatar(
//                        modifier = Modifier
//                            .padding(8.dp)
//                            .size(50.dp),
//                        user = currentUser,
//                        contentDescription = currentUser.name,
//                        showOnlineIndicator = false,
//                        onClick = { onBackPressed() }
//                    )

                    Image(
                        painter = painterResource(R.drawable.padlock),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(150.dp)
                            .align(Alignment.CenterHorizontally)
                            .dashedBorder(
                                width = 3.dp,
                                color = Color.Red,
                                shape = CircleShape,
                                on = 10.dp,
                                off = 3.dp
                            )
                            .padding(20.dp)
                            .clip(CircleShape)
                    )
                    
                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = currentUser.name,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                    )

                    Text(
                        text = simpleDateFormat(currentUser.createdAt),
                        fontSize = 17.sp,
                        color = Color.Gray,
                        style = MaterialTheme.typography.h2,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    private fun AppTopBarPreview() {
        UserDetailsTopAppBar(onBackButtonClicked = { /*TODO*/ }, user = User(id = "local", name = "anhhuy007"))
    }

    @Preview(showBackground = true)
    @Composable
    private fun HeaderInfPreview() {
        UserAvatarAndOverall(currentUser = User(id = "local", name = "anhhuy007", createdAt = Date(2022-1901, 12, 3)))
    }

    @SuppressLint("SimpleDateFormat")
    private fun simpleDateFormat(date: Date?): String {
        return SimpleDateFormat("EEE, MMM d, yyyy").format(date!!) ?: "Unknown created date"
    }

    companion object {
        private const val KEY_USER_ID = "userId"

        fun getUserIdIntent(context: Context, userId: String?) : Intent {
            return Intent(context, UserDetailsActivity::class.java).apply {
                putExtra(KEY_USER_ID, userId)
            }
        }
    }
}

fun Modifier.dashedBorder(border: BorderStroke, shape: Shape = RectangleShape, on: Dp, off: Dp) =
    dashedBorder(width = border.width, brush = border.brush, shape = shape, on, off)

/**
 * Returns a [Modifier] that adds border with appearance specified with [width], [color] and a
 * [shape], pads the content by the [width] and clips it.
 *
 * @sample androidx.compose.foundation.samples.BorderSampleWithDataClass()
 *
 * @param width width of the border. Use [Dp.Hairline] for a hairline border.
 * @param color color to paint the border with
 * @param shape shape of the border
 * @param on the size of the solid part of the dashes
 * @param off the size of the space between dashes
 */
fun Modifier.dashedBorder(width: Dp, color: Color, shape: Shape = RectangleShape, on: Dp, off: Dp) =
    dashedBorder(width, SolidColor(color), shape, on, off)

/**
 * Returns a [Modifier] that adds border with appearance specified with [width], [brush] and a
 * [shape], pads the content by the [width] and clips it.
 *
 * @sample androidx.compose.foundation.samples.BorderSampleWithBrush()
 *
 * @param width width of the border. Use [Dp.Hairline] for a hairline border.
 * @param brush brush to paint the border with
 * @param shape shape of the border
 */
fun Modifier.dashedBorder(width: Dp, brush: Brush, shape: Shape, on: Dp, off: Dp): Modifier =
    composed(
        factory = {
            this.then(
                Modifier.drawWithCache {
                    val outline: Outline = shape.createOutline(size, layoutDirection, this)
                    val borderSize = if (width == Dp.Hairline) 1f else width.toPx()

                    var insetOutline: Outline? = null // outline used for roundrect/generic shapes
                    var stroke: Stroke? = null // stroke to draw border for all outline types
                    var pathClip: Path? = null // path to clip roundrect/generic shapes
                    var inset = 0f // inset to translate before drawing the inset outline
                    // path to draw generic shapes or roundrects with different corner radii
                    var insetPath: Path? = null
                    if (borderSize > 0 && size.minDimension > 0f) {
                        if (outline is Outline.Rectangle) {
                            stroke = Stroke(
                                borderSize, pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(on.toPx(), off.toPx())
                                )
                            )
                        } else {
                            // Multiplier to apply to the border size to get a stroke width that is
                            // large enough to cover the corners while not being too large to overly
                            // square off the internal shape. The resultant shape will be
                            // clipped to the desired shape. Any value lower will show artifacts in
                            // the corners of shapes. A value too large will always square off
                            // the internal shape corners. For example, for a rounded rect border
                            // a large multiplier will always have squared off edges within the
                            // inner section of the stroke, however, having a smaller multiplier
                            // will still keep the rounded effect for the inner section of the
                            // border
                            val strokeWidth = 1.2f * borderSize
                            inset = borderSize - strokeWidth / 2
                            val insetSize = Size(
                                size.width - inset * 2,
                                size.height - inset * 2
                            )
                            insetOutline = shape.createOutline(insetSize, layoutDirection, this)
                            stroke = Stroke(
                                strokeWidth, pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(on.toPx(), off.toPx())
                                )
                            )
                            pathClip = if (outline is Outline.Rounded) {
                                Path().apply { addRoundRect(outline.roundRect) }
                            } else if (outline is Outline.Generic) {
                                outline.path
                            } else {
                                // should not get here because we check for Outline.Rectangle
                                // above
                                null
                            }

                            insetPath =
                                if (insetOutline is Outline.Rounded &&
                                    !insetOutline.roundRect.isSimple
                                ) {
                                    // Rounded rect with non equal corner radii needs a path
                                    // to be pre-translated
                                    Path().apply {
                                        addRoundRect(insetOutline.roundRect)
                                        translate(Offset(inset, inset))
                                    }
                                } else if (insetOutline is Outline.Generic) {
                                    // Generic paths must be created and pre-translated
                                    Path().apply {
                                        addPath(insetOutline.path, Offset(inset, inset))
                                    }
                                } else {
                                    // Drawing a round rect with equal corner radii without
                                    // usage of a path
                                    null
                                }
                        }
                    }

                    onDrawWithContent {
                        drawContent()
                        // Only draw the border if a have a valid stroke parameter. If we have
                        // an invalid border size we will just draw the content
                        if (stroke != null) {
                            if (insetOutline != null && pathClip != null) {
                                val isSimpleRoundRect = insetOutline is Outline.Rounded &&
                                        insetOutline.roundRect.isSimple
                                withTransform({
                                    clipPath(pathClip)
                                    // we are drawing the round rect not as a path so we must
                                    // translate ourselves othe
                                    if (isSimpleRoundRect) {
                                        translate(inset, inset)
                                    }
                                }) {
                                    if (isSimpleRoundRect) {
                                        // If we don't have an insetPath then we are drawing
                                        // a simple round rect with the corner radii all identical
                                        val rrect = (insetOutline as Outline.Rounded).roundRect
                                        drawRoundRect(
                                            brush = brush,
                                            topLeft = Offset(rrect.left, rrect.top),
                                            size = Size(rrect.width, rrect.height),
                                            cornerRadius = rrect.topLeftCornerRadius,
                                            style = stroke
                                        )
                                    } else if (insetPath != null) {
                                        drawPath(insetPath, brush, style = stroke)
                                    }
                                }
                                // Clip rect to ensure the stroke does not extend the bounds
                                // of the composable.
                                clipRect {
                                    // Draw a hairline stroke to cover up non-anti-aliased pixels
                                    // generated from the clip
                                    if (isSimpleRoundRect) {
                                        val rrect = (outline as Outline.Rounded).roundRect
                                        drawRoundRect(
                                            brush = brush,
                                            topLeft = Offset(rrect.left, rrect.top),
                                            size = Size(rrect.width, rrect.height),
                                            cornerRadius = rrect.topLeftCornerRadius,
                                            style = Stroke(
                                                Stroke.HairlineWidth,
                                                pathEffect = PathEffect.dashPathEffect(
                                                    floatArrayOf(on.toPx(), off.toPx())
                                                )
                                            )
                                        )
                                    } else {
                                        drawPath(
                                            pathClip, brush = brush, style = Stroke(
                                                Stroke.HairlineWidth,
                                                pathEffect = PathEffect.dashPathEffect(
                                                    floatArrayOf(on.toPx(), off.toPx())
                                                )
                                            )
                                        )
                                    }
                                }
                            } else {
                                // Rectangular border fast path
                                val strokeWidth = stroke.width
                                val halfStrokeWidth = strokeWidth / 2
                                drawRect(
                                    brush = brush,
                                    topLeft = Offset(halfStrokeWidth, halfStrokeWidth),
                                    size = Size(
                                        size.width - strokeWidth,
                                        size.height - strokeWidth
                                    ),
                                    style = stroke
                                )
                            }
                        }
                    }
                }
            )
        },
        inspectorInfo = debugInspectorInfo {
            name = "border"
            properties["width"] = width
            if (brush is SolidColor) {
                properties["color"] = brush.value
                value = brush.value
            } else {
                properties["brush"] = brush
            }
            properties["shape"] = shape
        }
    )


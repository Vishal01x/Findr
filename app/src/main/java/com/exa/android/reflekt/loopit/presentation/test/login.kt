
import android.content.res.Configuration
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import kotlin.math.roundToInt

// -------------------- COLORS --------------------

private val BackgroundDark = Color(0xFF101D22)
private val CardDark = Color(0xFF16262C)
private val Primary = Color(0xFF13B6EC)
private val PrimaryDark = Color(0xFF0EA0D1)

// -------------------- THEME --------------------

@Composable
fun CampusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Primary,
            background = BackgroundDark,
            surface = CardDark,
            onPrimary = Color.White,
            onBackground = Color.White
        ),
        content = content
    )
}

// -------------------- MAIN SCREEN --------------------

@Composable
fun CampusLoginScreen() {

    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {

        // Dotted pattern
        PatternBackground()

        // Glow blobs
        GlowCircle(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-80).dp),
            size = 250.dp
        )

        GlowCircle(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-120).dp),
            size = 300.dp,
            alpha = 0.08f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(90.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(20.dp, RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Primary, Color(0xFF2563EB))
                        ),
                        RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.School,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                buildAnnotatedString {
                    append("Campus")
                    addStyle(
                        SpanStyle(color = Primary),
                        6,
                        13
                    )
                    append("Connect")
                },
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Connect. Collaborate.\nGet Hired.",
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Glass Card
            GlassCard {

                // Google Button
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Icon(Icons.Outlined.AccountCircle, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue with Google", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(18.dp))

                DividerWithText("or continue with email")

                Spacer(modifier = Modifier.height(18.dp))

                // Email
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Student Email (.edu)") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Email, null)
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Password
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Password") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Lock, null)
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            passwordVisible = !passwordVisible
                        }) {
                            Icon(
                                if (passwordVisible)
                                    Icons.Outlined.Visibility
                                else
                                    Icons.Outlined.VisibilityOff,
                                null
                            )
                        }
                    },
                    visualTransformation =
                        if (passwordVisible)
                            VisualTransformation.None
                        else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Forgot password?",
                    color = Primary,
                    modifier = Modifier.align(Alignment.End),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Sign In
                Button(
                    onClick = {},
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    )
                ) {
                    Text("Sign In", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Outlined.ArrowForward, null)
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                "New to CampusConnect? Create an account",
                color = Color(0xFF94A3B8),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        // Decorative bottom blob
        GlowCircle(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 80.dp),
            size = 260.dp,
            alpha = 0.1f
        )
    }
}

// -------------------- GLASS CARD --------------------

@Composable
fun GlassCard(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardDark.copy(alpha = 0.7f))
            .border(
                1.dp,
                Primary.copy(alpha = 0.1f),
                RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Column(content = content)
    }
}

// -------------------- DIVIDER --------------------

@Composable
fun DividerWithText(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Divider(
            modifier = Modifier.weight(1f),
            color = Color.Gray.copy(alpha = 0.3f)
        )
        Text(
            text,
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Divider(
            modifier = Modifier.weight(1f),
            color = Color.Gray.copy(alpha = 0.3f)
        )
    }
}

// -------------------- BACKGROUND PATTERN --------------------

@Composable
fun PatternBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val spacing = 40.dp.toPx()
        val radius = 1.5.dp.toPx()

        val rows = (size.height / spacing).roundToInt()
        val cols = (size.width / spacing).roundToInt()

        for (i in 0..rows) {
            for (j in 0..cols) {
                drawCircle(
                    color = Primary.copy(alpha = 0.15f),
                    radius = radius,
                    center = Offset(j * spacing, i * spacing)
                )
            }
        }
    }
}

// -------------------- GLOW --------------------

@Composable
fun GlowCircle(
    modifier: Modifier,
    size: Dp,
    alpha: Float = 0.2f
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Primary.copy(alpha = alpha),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            )
    )
}

// -------------------- PREVIEW --------------------

@Preview(
    //showBackground = true,
   // uiMode = Configuration.UI_MODE_NIGHT_YES,
    showSystemUi = true
)
@Composable
fun CampusPreview() {
    CampusTheme {
        CampusLoginScreen()
    }
}

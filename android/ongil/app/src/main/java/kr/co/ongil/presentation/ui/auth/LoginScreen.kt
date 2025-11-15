package kr.co.ongil.presentation.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.ongil.R
import kr.co.ongil.presentation.theme.OngilTheme

// ---- 로컬 컬러 정의 (이 파일에서만 사용) ----
private val BrandGreen   = Color(0xFF8CA898)
private val TitleGray    = Color(0xFF2B3A34)
private val HintGray     = Color(0xFF9CA3AF)
private val FieldBorder  = Color(0xFFE7EBEF)
private val DividerGray  = Color(0xFFEFEFEF)
private val KakaoYellow  = Color(0xFFFEE500)
private val GoogleBorder = Color(0xFFE5E7EB)

@Composable
fun LoginScreen(
    state: LoginUiState,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onClickFindPw: () -> Unit = {},
    onClickSignup: () -> Unit = {},
    onClickKakao: () -> Unit = {},
    onClickGoogle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 비밀번호 보기/숨기기 토글은 로컬 UI 상태로 유지
    var pwVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 타이틀
        Text(
            text = "로그인",
            fontSize = 36.sp,
            fontWeight = FontWeight.SemiBold,
            color = TitleGray,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "안전한 돌봄을 위한 스마트 케어",
            fontSize = 14.sp,
            color = HintGray,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        // 전화번호 입력
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("전화번호", color = TitleGray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = state.phone,
                onValueChange = onPhoneChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                singleLine = true,
                placeholder = { Text("전화번호를 입력해주세요", color = HintGray, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "전화번호 아이콘",
                        tint = BrandGreen
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandGreen,
                    unfocusedBorderColor = FieldBorder,
                    cursorColor = BrandGreen
                )
            )
        }

        Spacer(Modifier.height(16.dp))

        // 비밀번호 입력
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("비밀번호", color = TitleGray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                singleLine = true,
                placeholder = { Text("비밀번호를 입력해주세요", color = HintGray, fontSize = 14.sp) },
                visualTransformation = if (pwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "비밀번호 아이콘",
                        tint = BrandGreen
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { pwVisible = !pwVisible }) {
                        Icon(
                            imageVector = if (pwVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = "비밀번호 보기 전환",
                            tint = HintGray
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                keyboardActions = KeyboardActions(onDone = { onLoginClick() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandGreen,
                    unfocusedBorderColor = FieldBorder,
                    cursorColor = BrandGreen
                )
            )
        }

        Spacer(Modifier.height(10.dp))

        // 비밀번호 찾기 / 회원가입
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onClickFindPw) {
                Text("비밀번호 찾기", color = TitleGray, fontSize = 13.sp)
            }
            TextButton(onClick = onClickSignup) {
                Text("회원가입", color = BrandGreen, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(12.dp))

        // 로그인 버튼
        Button(
            onClick = onLoginClick,
            enabled = state.isLoginEnabled && !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
            shape = MaterialTheme.shapes.medium
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp),
                    color = Color.White
                )
                Spacer(Modifier.width(8.dp))
            }
            Text("로그인", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }

        Spacer(Modifier.height(28.dp))

        // 구분선
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(modifier = Modifier.weight(1f), color = DividerGray, thickness = 1.dp)
            Text("  또는  ", color = HintGray, fontSize = 12.sp)
            Divider(modifier = Modifier.weight(1f), color = DividerGray, thickness = 1.dp)
        }

        Spacer(Modifier.height(16.dp))

        // 카카오 로그인
        OutlinedButton(
            onClick = onClickKakao,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = KakaoYellow),
            border = null,
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_kakao), // drawable/ic_kakao.xml
                contentDescription = "카카오 로고",
                modifier = Modifier.size(55.dp),
                tint = Color.Unspecified
            )
            Spacer(Modifier.width(1.dp))
            Text("카카오로 시작하기", color = TitleGray, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(10.dp))

        // 구글 로그인
        OutlinedButton(
            onClick = onClickGoogle,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.background),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(GoogleBorder)),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google), // drawable/ic_google.xml
                contentDescription = "Google 로고",
                modifier = Modifier.size(18.dp),
                tint = Color.Unspecified
            )
            Spacer(Modifier.width(20.dp))
            Text("구글로 시작하기", color = TitleGray, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(12.dp))
    }
}

/* ============== 프리뷰 ============== */
@Preview(showBackground = true, name = "Login - Light")
@Composable
private fun PreviewLoginLight() {
    OngilTheme(darkTheme = false) {
        LoginScreen(
            state = LoginUiState(), // 기본값(비활성)
            onPhoneChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onClickFindPw = {},
            onClickSignup = {},
            onClickKakao = {},
            onClickGoogle = {}
        )
    }
}

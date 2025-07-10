package com.example.spendify_v1.ui.screen.signUp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@Composable
fun SignUpScreen(
    navController: NavHostController,
    viewModel: SignUpViewModel = hiltViewModel()
) {

    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confermaPassword by remember { mutableStateOf("") }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Spendify",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Registra il tuo account adesso"
        )
        Spacer(modifier = Modifier.height(15.dp))
        OutlinedTextField(
            value = nome,
            onValueChange = {
                nome = it
                viewModel.resettaErrori()
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Nome")},
            leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = "Email Icon")},
            shape = RoundedCornerShape(16.dp),
            isError = viewModel.nomeError != null
        )
        if (viewModel.nomeError != null) {
            Text(
                text = viewModel.nomeError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                viewModel.resettaErrori()
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Email")},
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon")},
            shape = RoundedCornerShape(16.dp),
            isError = viewModel.emailError != null
        )
        if (viewModel.emailError != null) {
            Text(
                text = viewModel.emailError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                viewModel.resettaErrori()
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Password")},
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Email Icon")},
            shape = RoundedCornerShape(16.dp),
            visualTransformation = PasswordVisualTransformation(),
            isError = viewModel.passwordError != null
        )
        if (viewModel.passwordError != null) {
            Text(
                text = viewModel.passwordError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = confermaPassword,
            onValueChange = {
                confermaPassword = it
                viewModel.resettaErrori()
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Conferma password")},
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Email Icon")},
            shape = RoundedCornerShape(16.dp),
            visualTransformation = PasswordVisualTransformation(),
            isError = viewModel.confermaPasswordError != null
        )
        if (viewModel.confermaPasswordError != null){
            Text(
                text = viewModel.confermaPasswordError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                // if per evitare spam di click
                if (!viewModel.caricamento) {
                    viewModel.signUp(nome, email, password, confermaPassword)
                }
            },
            shape = RoundedCornerShape(20.dp),
            enabled = !viewModel.caricamento
        ) {
            if (viewModel.caricamento) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(2.dp),
                    strokeWidth = 4.dp
                )
            } else {
                Text("Registrati")
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row (
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Hai già un account?")
            TextButton(
                onClick = {
                    navController.navigate("login")
                }
            ) {
                Text("Accedi")
            }
        }
    }
    if (viewModel.showSuccessDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { viewModel.chiudiDialogSuccesso() },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.chiudiDialogSuccesso()
                    navController.navigate("main") {
                        popUpTo("auth") {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }) {
                    Text("Continua")
                }
            },
            title = { Text("Registrazione completata") },
            text = { Text("Il tuo account è stato creato con successo!") }
        )
    }
}
<script setup>
import { ref } from 'vue';

const emit = defineEmits(['login-success']);

const isLoginMode = ref(true);
const login = ref('');
const password = ref('');
const message = ref('');

const toggleMode = () => {
  isLoginMode.value = !isLoginMode.value;
  message.value = '';
};

const handleSubmit = async () => {
  message.value = '';
  const endpoint = isLoginMode.value ? '/api/auth/login' : '/api/auth/register';
  
  try {
    const response = await fetch(endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include', // Important for cookies
      body: JSON.stringify({
        login: login.value,
        password: password.value
      })
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || 'Wystąpił błąd.');
    }

    if (isLoginMode.value) {
      // Login success
      emit('login-success', { username: login.value, token: data.token });
    } else {
      // Register success
      message.value = 'Rejestracja udana! Możesz się teraz zalogować.';
      isLoginMode.value = true;
    }
  } catch (error) {
    console.error(error);
    message.value = error.message;
  }
};
</script>

<template>
  <div class="auth-container">
    <h2>{{ isLoginMode ? 'Logowanie' : 'Rejestracja' }}</h2>
    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label>Login:</label>
        <input v-model="login" type="text" required />
      </div>
      <div class="form-group">
        <label>Hasło:</label>
        <input v-model="password" type="password" required />
      </div>
      <button type="submit">{{ isLoginMode ? 'Zaloguj' : 'Zarejestruj' }}</button>
    </form>
    <p v-if="message" class="message">{{ message }}</p>
    <p class="toggle-text">
      {{ isLoginMode ? 'Nie masz konta?' : 'Masz już konto?' }}
      <button @click="toggleMode" class="link-btn">
        {{ isLoginMode ? 'Zarejestruj się' : 'Zaloguj się' }}
      </button>
    </p>
  </div>
</template>

<style scoped>
.auth-container {
  max-width: 350px;
  width: 100%;
  padding: 2.5rem;
  border-radius: 16px;
  background-color: #ffffff;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
  text-align: center;
}

h2 {
  margin-top: 0;
  margin-bottom: 1.5rem;
  color: #333;
  font-size: 1.8rem;
}

.form-group {
  margin-bottom: 1.2rem;
  text-align: left;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #555;
  font-size: 0.9rem;
}

input {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

input:focus {
  outline: none;
  border-color: #588317; /* var(--dark-green) */
}

button[type="submit"] {
  width: 100%;
  padding: 0.8rem;
  background-color: #588317; /* var(--dark-green) */
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.2s, transform 0.1s;
  margin-top: 0.5rem;
}

button[type="submit"]:hover {
  background-color: #4a6e13;
}

button[type="submit"]:active {
  transform: scale(0.98);
}

.toggle-text {
  margin-top: 1.5rem;
  font-size: 0.9rem;
  color: #666;
}

.link-btn {
  background: none;
  border: none;
  color: #588317;
  font-weight: bold;
  cursor: pointer;
  padding: 0;
  margin-left: 5px;
  font-size: 0.9rem;
}

.link-btn:hover {
  text-decoration: underline;
}

.message {
  margin-top: 1rem;
  padding: 0.5rem;
  border-radius: 4px;
  background-color: #ffebee;
  color: #c62828;
  font-size: 0.9rem;
}
</style>

<script setup>
import { ref, computed, onMounted, shallowRef } from 'vue';
import { io } from 'socket.io-client';
import AuthForm from './components/AuthForm.vue';
import GameBoard from './components/GameBoard.vue';
import MoveControls from './components/MoveControls.vue';
import RankingView from './components/RankingView.vue';
import LobbyView from './components/LobbyView.vue';
import MyGamesView from './components/MyGamesView.vue';

const isLoggedIn = ref(false);
const socket = ref(null);
const user = ref(null);
const currentView = ref('lobby'); // 'lobby', 'game', 'my-games', 'ranking'
const recievedMove =  ref(null);
const moveHistory = shallowRef([]);
const color = ref(null);
const errorMessage = ref(null);

// Game Board State (Map: "A1" -> "white" | "red")
const board = ref({});

const updateBoardState = (boardState) => {
  board.value = {};

  boardState.biale.forEach(pos => board.value[pos] = 'white');
  boardState.czarne.forEach(pos => board.value[pos] = 'red');
};

const connectSocket = () => {
    socket.value = io({
        withCredentials: true
    });
    
    socket.value.on('connect', () => {
        console.log('Connected to server');
    });

    socket.value.on('connected_to_game', ({ moveHistory: moves, color: assignedColor }) => {
        moveHistory.value = moves;
        color.value = assignedColor;
        currentView.value = 'game';
    });

    socket.value.on('error', (error) => {
        console.error(error);
        errorMessage.value = error || "Wystąpił nieznany błąd";
    });

    socket.value.on('connect_error', (err) => {
        console.error("Błąd połączenia:", err.message); // np. "Niezalogowany"
    });

    socket.value.on('move_made', (move) => {
      recievedMove.value = move.ruch;
    });

    socket.value.on('game_over', (data) => {
      errorMessage.value = `Koniec gry! Zwycięzca: ${data.zwyciezca_login}`;
    });
};

const handleLoginSuccess = (userData) => {
  user.value = userData;
  isLoggedIn.value = true;
  currentView.value = 'lobby';
  connectSocket();
};

const checkSession = async () => {
    try {
        const response = await fetch('/api/auth/me', {
            credentials: 'include'
        });
        if (response.ok) {
            const userData = await response.json();
            user.value = userData;
            isLoggedIn.value = true;
            connectSocket();
        }
    } catch (e) {
        console.log("Not logged in");
    }
};

const createGame = async () => {
  if (currentView.value === 'game') return;
  try {
    const response = await fetch('/api/game/new', {
      method: 'POST',
      credentials: 'include'
    });
    const data = await response.json();
    if (response.ok) {
       // Automatically join the game view
       // We might need to store current game ID in a ref
       console.log("Created game:", data.id_gra);// Initialize visuals
       connectToGame(data.id_gra);
    } else {
       errorMessage.value = "Błąd tworzenia gry: " + response.status;
    }
  } catch(e) {
    console.error(e);
    errorMessage.value = "Błąd połączenia: " + e.message;
  }
};

const connectToGame = (id_gra) => {
  if (socket.value) {
    socket.value.emit('connect_to_game', { id_gra: id_gra });
    console.log('Connecting to game:', id_gra);
  }
};

const disconnectFromGame = () => {
  if (socket.value) {
    socket.value.emit('disconnect_from_game');
    console.log('Disconnected from game');
  }
};

const exitGame = () => {
  currentView.value = 'lobby';
  errorMessage.value = null;
};

onMounted(() => {
    checkSession();
});

const handleSendMove = (move) => {
  if (socket.value) {
    socket.value.emit('make_move', move);
    console.log('Sent move:', move);
  }
};

const logout = async () => {
  try {
      await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' });
      socket.value?.disconnect();
  } catch (e) {
      console.error(e);
  }
  
  isLoggedIn.value = false;
  user.value = null;
  if (socket.value) {
    socket.value.disconnect();
    socket.value = null;
  }
  currentView.value = 'login';
};

const isMenuOpen = ref(false);

const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value;
};

const setView = (view) => {
  currentView.value = view;
  isMenuOpen.value = false; // Close menu on selection (mobile UX)
};

const createGameAndClose = () => {
  createGame();
  isMenuOpen.value = false;
};

const logoutAndClose = () => {
  logout();
  isMenuOpen.value = false;
};
</script>

<template>
  <div class="page-root">
    
    <div class="center-wrapper">
      <!-- Sidebar -->
      <aside v-if="isLoggedIn" class="sidebar" :class="{ 'menu-open': isMenuOpen }">
        <div class="mobile-header">
           <h1>Warcaby</h1>
           <button class="menu-toggle" @click="toggleMenu">
             <span class="bar"></span>
             <span class="bar"></span>
             <span class="bar"></span>
           </button>
        </div>

        <nav :class="{ 'nav-hidden': !isMenuOpen }">
          <h1 class="desktop-title">Warcaby</h1>
          <ul>
              <li><a href="#" @click.prevent="createGameAndClose" class="create-game" draggable="false" :disabled="currentView === 'game'">Utwórz Grę</a></li>
              <li><a href="#" @click.prevent="setView('lobby')" :class="{ active: currentView === 'lobby' }" draggable="false">Lobby</a></li>
              <li><a href="#" @click.prevent="setView('my-games')" :class="{ active: currentView === 'my-games' }" draggable="false">Moje Gry</a></li>
              <li><a href="#" @click.prevent="setView('ranking')" :class="{ active: currentView === 'ranking' }" draggable="false">Ranking</a></li>
              <li><a href="#" @click.prevent="logoutAndClose" class="logout-btn" draggable="false">Wyloguj</a></li>
          </ul>
          
          <div class="user-info">
            Zalogowany: <strong>{{ user?.username }}</strong>
          </div>
        </nav>
      </aside>

      <!-- Main Content Area -->
      <main class="content">
        <!-- Auth View -->
        <div v-if="!isLoggedIn" class="auth-wrapper">
          <h1 class="main-title">Warcaby Online</h1>
          <AuthForm @login-success="handleLoginSuccess" />
        </div>

        <!-- Authenticated Views -->
        <div v-else class="game-area">
          
          <!-- Lobby View -->
          <div v-if="currentView === 'lobby'" class="view-container">
            <LobbyView @join-success="connectToGame" />
          </div>

          <div v-else-if="currentView === 'ranking'" class="view-container">
            <RankingView />
          </div>

          <div v-else-if="currentView === 'my-games'" class="view-container">
            <MyGamesView @rejoin-game="connectToGame" />
          </div>

          <!-- Game View -->
          <div v-else-if="currentView === 'game'" class="game-container">
            
            <div v-if="errorMessage" class="game-error-overlay">
                <div class="game-error-box">
                    <p>{{ errorMessage }}</p>
                    <div class="game-error-buttons">
                        <button @click="errorMessage = null">Zamknij</button>
                        <button @click="exitGame" class="danger-btn">Wyjdź z gry</button>
                    </div>
                </div>
            </div>

            <div class="board-wrapper">
              <GameBoard :pieces="board" :my-color="color"/>
            </div>
            <div class="controls-wrapper">
              <MoveControls 
                @send-move="handleSendMove" 
                @disconnect-from-game="disconnectFromGame"
                @board-state="updateBoardState" 
                :move="recievedMove"
                :moveHistory="moveHistory"
                :my-color="color"
              />
            </div>
          </div>

        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* Layout structure adhering to index.css concepts */

.page-root {
  height: 100vh; /* Fixed viewport height */
  display: flex;
  flex-direction: column;
  overflow: hidden; /* Prevent body scroll */
}

.center-wrapper {
  flex: 1; /* Take remaining space */
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  gap: 24px;
  overflow: hidden; /* Content inside should scroll if needed */
  min-height: 0; /* Crucial for flex nested scrolling */
}

.sidebar {
  width: 220px;
  height: calc(100vh - 80px); /* Fill center-wrapper */
  background: var(--dark-green);
  border-radius: 12px;
  box-shadow: 0 8px 20px rgba(16,24,40,0.06);
  padding: 18px;
  color: #fff;
  overflow-y: auto; /* Scrollable sidebar if needed */
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.mobile-header {
  display: none;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.menu-toggle {
  display: none;
  background: none;
  border: none;
  cursor: pointer;
  padding: 5px;
  flex-direction: column;
  gap: 4px;
}

.bar {
  display: block;
  width: 25px;
  height: 3px;
  background-color: white;
  border-radius: 2px;
}

.sidebar nav {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
}

.sidebar h1.desktop-title {
  margin-top: 0;
  text-align: center;
  margin-bottom: 2rem;
  font-size: 1.5rem;
}

.sidebar h1 {
   margin: 0; 
}

.sidebar nav ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sidebar nav a {
  display: block;
  color: #eee;
  text-decoration: none;
  font-weight: 600;
  font-size: 1.05rem;
  padding: 12px 16px;
  border-radius: 8px;
  transition: all 0.2s ease;
  user-select: none;
}

.sidebar nav a:hover, .sidebar nav a.active {
  background-color: rgba(255, 255, 255, 0.15);
  color: #fff;
  transform: translateX(4px);
  text-decoration: none;
}

.create-game {
  background-color: var(--board-dark);
  color: #fff !important;
}

.logout-btn {
  margin-top: auto;
  margin-bottom: 10px;
  color: #ffcccc !important;
}

.logout-btn:hover {
  background-color: rgba(255, 0, 0, 0.15) !important;
  color: #ff8888 !important;
}

.user-info {
  margin-top: auto; /* Push to bottom if flex column */
  padding-top: 1rem;
  border-top: 1px solid rgba(255,255,255,0.2);
  font-size: 0.9rem;
}

.content {
  /* Dynamic width content area */
  flex: 1;
  max-width: 1144px;
  height: calc(100vh - 104px);
  background: var(--dark-green);
  border-radius: 12px;
  box-shadow: 0 12px 30px rgba(16,24,40,0.06);
  padding: 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
  /* justify-content: center;  Remove this to allow top alignment for lists */
  overflow-y: auto; /* Allow scroll if content is too tall */
  transition: all 0.3s ease;
}

.auth-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 2rem;
}

.main-title {
  font-size: 3rem;
  color: #fff;
  text-shadow: 0 4px 6px rgba(0,0,0,0.2);
  margin: 0;
  font-weight: 800;
  letter-spacing: 1px;
}

.game-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  width: 100%;
}

.view-placeholder h2 {
  font-size: 2rem;
  margin-bottom: 1rem;
}

.view-container {
  width: 100%;
  display: flex;
  justify-content: center;
}

.game-container {
  display: flex;
  gap: 20px;
  width: 100%;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  position: relative; /* Anchor for absolute overlay */
}

.game-error-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.65);
  display: flex;
  justify-content: center;
  align-items: flex-start; /* Position slightly higher */
  padding-top: 20%;
  z-index: 999;
  border-radius: 8px;
  backdrop-filter: blur(2px);
}

.game-error-box {
  background: white;
  padding: 24px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 10px 25px rgba(0,0,0,0.5);
  max-width: 90%;
  width: 400px;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from { transform: translateY(-20px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.game-error-box p {
  color: #d32f2f;
  font-weight: 600;
  margin-bottom: 24px;
  font-size: 1.1rem;
}

.game-error-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.game-error-buttons button {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.95rem;
  transition: background-color 0.2s;
}

.game-error-buttons button:first-child {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
}

.game-error-buttons button:first-child:hover {
  background-color: #e0e0e0;
}

.game-error-buttons .danger-btn {
  background-color: #d32f2f;
  color: white;
}

.game-error-buttons .danger-btn:hover {
  background-color: #b71c1c;
}

.controls-wrapper {
  width: min(72vmin, 60vh, 680px); /* Match board width */
  /* margin-top: 20px; */
}

@media (max-width: 800px) {
  .center-wrapper {
    flex-direction: column;
    padding: 10px;
    gap: 10px;
  }

  .sidebar {
    width: 100%;
    height: auto;
    min-height: 60px;
    padding: 10px 20px;
    overflow: hidden;
  }

  .mobile-header {
    display: flex;
  }
  
  .menu-toggle {
    display: flex;
  }

  .desktop-title {
    display: none;
  }

  .sidebar nav {
    /* Hidden by default on mobile */
    display: none; 
    margin-top: 1rem;
    border-top: 1px solid rgba(255,255,255,0.1);
    padding-top: 10px;
  }

  /* Show nav when menu is open */
  .sidebar.menu-open nav {
    display: flex;
  }
  
  /* When menu is open, allow sidebar to grow */
  .sidebar.menu-open {
    height: auto;
  }

  .content {
    width: 100%;
    height: auto;
    flex: 1;
    padding: 15px;
  }
  
  /* Ensure main content scrolls properly in column mode */
  .page-root {
    overflow-y: auto;
  }
  
  .center-wrapper {
    height: auto;
    min-height: 100vh;
  }
}
</style>

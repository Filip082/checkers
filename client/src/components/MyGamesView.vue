<script setup>
import { ref, onMounted } from 'vue';

const emit = defineEmits(['rejoin-game']);

const games = ref([]);
const loading = ref(true);
const error = ref('');

const fetchMyGames = async () => {
  try {
    const response = await fetch('/api/game/my', {
      credentials: 'include'
    });
    
    if (!response.ok) throw new Error('Nie udało się pobrać listy gier.');
    
    const data = await response.json();
    games.value = data.games;
    console.log(data.games);
  } catch (err) {
    error.value = err.message;
  } finally {
    loading.value = false;
  }
};

const rejoinGame = (id_gra) => {
  // Logic to switch view to game board with this game ID
  emit('rejoin-game', id_gra);
};

const isActive = (status) => {
  return status === 'Utworzona' || status === 'Trwająca';
};

onMounted(() => {
  fetchMyGames();
});
</script>

<template>
  <div class="my-games-card">
    <div class="header">
      <h2>Twoje Gry</h2>
      <button @click="fetchMyGames" class="refresh-btn">Odśwież</button>
    </div>
    
    <div v-if="loading" class="status">Ładowanie...</div>
    <div v-else-if="error" class="status error">{{ error }}</div>
    
    <div v-else class="table-wrapper">
      <table class="games-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Przeciwnik</th>
            <th>Twój Kolor</th>
            <th>Status</th>
            <th>Wynik</th>
            <th>Akcja</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="game in games" :key="game.id_gra" :class="{ 'my-turn-row': game.moja_kolej }">
            <td class="id-col">#{{ game.id_gra }}</td>
            <td class="opponent-col">{{ game.przeciwnik }}</td>
            <td class="color-col" :class="game.moj_kolor">
              <span>{{ game.moj_kolor }}</span>
            </td>
            <td class="status-col">
              {{ game.status_gry }}
              <span v-if="game.moja_kolej" class="turn-badge">Twój ruch</span>
            </td>
            <td class="result-col">{{ game.wynik }}</td>
            <td class="action-col">
              <button 
                v-if="isActive(game.status_gry)" 
                @click="rejoinGame(game.id_gra)" 
                class="rejoin-btn"
              >
                Wróć do gry
              </button>
              <span v-else class="closed-msg">-</span>
            </td>
          </tr>
          <tr v-if="games.length === 0">
            <td colspan="6" class="empty-msg">Nie brałeś udziału w żadnych grach.</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.my-games-card {
  width: 100%;
  max-width: 800px;
  padding: 2rem;
  border-radius: 12px;
  background-color: #ffffff;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
  box-sizing: border-box;
  min-width: 0; /* Crucial: allows container to shrink below content size in flexbox */
}

@media (max-width: 600px) {
  .my-games-card {
    padding: 1rem;
  }
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

h2 {
  margin: 0;
  color: #333;
}

.refresh-btn {
  background: none;
  border: 1px solid #ccc;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  color: #555;
  font-size: 0.9rem;
}

.refresh-btn:hover {
  background-color: #f0f0f0;
}

.status {
  padding: 1rem;
  text-align: center;
  color: #666;
}

.error {
  color: #c62828;
}

.table-wrapper {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch; /* Smooth scroll on iOS */
}

.games-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 600px; /* Force minimum width to trigger scroll */
}

.games-table th,
.games-table td {
  padding: 12px;
  border-bottom: 1px solid #eee;
  text-align: left;
}

.games-table th {
  background-color: #f8f9fa;
  color: #555;
  font-weight: 600;
}

.games-table tr:last-child td {
  border-bottom: none;
}

.my-turn-row {
  background-color: #e8f5e9;
}

.my-turn-row:hover {
  background-color: #c8e6c9;
}

.turn-badge {
  display: inline-block;
  background-color: #ff8000;
  color: white;
  font-size: 0.7em;
  padding: 2px 6px;
  border-radius: 10px;
  vertical-align: middle;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.id-col {
  width: 60px;
  color: #888;
  font-family: monospace;
}

.opponent-col {
  font-weight: 500;
  color: #333;
}

.color-col {
  text-transform: capitalize;
}

.color-col span {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 5px;
  margin: auto;
}

.color-col.Biały span { 
  color: var(--board-dark);
  font-weight: bold; 
  background-color: var(--board-light);
}
.color-col.Czarny span { 
  color: var(--board-light);
  font-weight: bold; 
  background-color: var(--board-dark);
}

.status-col {
  color: #666;
}

.result-col {
  color: #888;
}

.action-col {
  text-align: right;
  width: 120px;
}

.rejoin-btn {
  background-color: #588317;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.2s;
  font-size: 0.85rem;
}

.rejoin-btn:hover {
  background-color: #4a6e13;
}

.empty-msg {
  text-align: center;
  color: #999;
  font-style: italic;
  padding: 2rem 0;
}
</style>

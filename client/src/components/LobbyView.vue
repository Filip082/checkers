<script setup>
import { ref, onMounted } from 'vue';

const emit = defineEmits(['join-success']);

const games = ref([]);
const loading = ref(true);
const error = ref('');

const fetchGames = async () => {
  try {
    const response = await fetch('/api/game/open', {
      credentials: 'include'
    });
    
    if (!response.status === 200) throw new Error('Nie udało się pobrać listy gier.');
    
    const data = await response.json();
    games.value = data.games;
  } catch (err) {
    error.value = err.message;
  } finally {
    loading.value = false;
  }
};

const joinGame = async (id_gra) => {
  try {
    const response = await fetch(`/api/game/${id_gra}/join`, {
      method: 'POST',
      credentials: 'include'
    });
    
    const data = await response.json();
    if (!response.status === 200) {
        throw new Error(data.error || 'Nie udało się dołączyć do gry.');
    }
    
    // Emit success event to parent to switch view
    emit('join-success', id_gra);
    
  } catch (err) {
    alert(err.message);
  }
};

onMounted(() => {
  fetchGames();
});
</script>

<template>
  <div class="lobby-card">
    <div class="header">
      <h2>Dostępne Gry</h2>
      <button @click="fetchGames" class="refresh-btn">Odśwież</button>
    </div>
    
    <div v-if="loading" class="status">Ładowanie...</div>
    <div v-else-if="error" class="status error">{{ error }}</div>
    
    <table v-else class="lobby-table">
      <thead>
        <tr>
          <th>ID Gry</th>
          <th>Przeciwnik</th>
          <th>Akcja</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="game in games" :key="game.id_gra">
          <td class="id-col">#{{ game.id_gra }}</td>
          <td class="player-col">{{ game.login }}</td>
          <td class="action-col">
            <button @click="joinGame(game.id_gra)" class="join-btn">Dołącz</button>
          </td>
        </tr>
        <tr v-if="games.length === 0">
          <td colspan="3" class="empty-msg">Brak otwartych gier. Utwórz własną!</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.lobby-card {
  width: 100%;
  max-width: 600px;
  padding: 2rem;
  border-radius: 12px;
  background-color: #ffffff;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
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

.lobby-table {
  width: 100%;
  border-collapse: collapse;
}

.lobby-table th,
.lobby-table td {
  padding: 12px;
  border-bottom: 1px solid #eee;
  text-align: left;
}

.lobby-table th {
  background-color: #f8f9fa;
  color: #555;
  font-weight: 600;
}

.lobby-table tr:last-child td {
  border-bottom: none;
}

.id-col {
  width: 80px;
  color: #888;
  font-family: monospace;
}

.player-col {
  font-weight: bold;
  color: #333;
}

.action-col {
  text-align: right;
  width: 100px;
}

.join-btn {
  background-color: #588317;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.2s;
}

.join-btn:hover {
  background-color: #4a6e13;
}

.empty-msg {
  text-align: center;
  color: #999;
  font-style: italic;
  padding: 2rem 0;
}
</style>

<script setup>
import { ref, onMounted } from 'vue';

const ranking = ref([]);
const loading = ref(true);
const error = ref('');

const fetchRanking = async () => {
  try {
    const response = await fetch('/api/game/ranking', {
      credentials: 'include'
    });
    
    if (!response.ok) throw new Error('Nie udało się pobrać rankingu.');
    
    const data = await response.json();
    ranking.value = data.ranking;
  } catch (err) {
    error.value = err.message;
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchRanking();
});
</script>

<template>
  <div class="ranking-card">
    <h2>Ranking Graczy</h2>
    
    <div v-if="loading" class="status">Ładowanie...</div>
    <div v-else-if="error" class="status error">{{ error }}</div>
    
    <table v-else class="ranking-table">
      <thead>
        <tr>
          <th>#</th>
          <th>Gracz</th>
          <th>Zwycięstwa</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(player, index) in ranking" :key="player.login">
          <td class="rank-col">{{ index + 1 }}</td>
          <td class="player-col">{{ player.login }}</td>
          <td class="wins-col">{{ player.liczba_zwyciestw }}</td>
        </tr>
        <tr v-if="ranking.length === 0">
          <td colspan="3" class="empty-msg">Brak danych w rankingu.</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.ranking-card {
  width: 100%;
  max-width: 600px;
  padding: 2rem;
  border-radius: 12px;
  background-color: #ffffff;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
  text-align: center;
}

h2 {
  margin-top: 0;
  margin-bottom: 1.5rem;
  color: #333;
}

.status {
  padding: 1rem;
  color: #666;
}

.error {
  color: #c62828;
}

.ranking-table {
  width: 100%;
  border-collapse: collapse;
}

.ranking-table th,
.ranking-table td {
  padding: 12px;
  border-bottom: 1px solid #eee;
  text-align: left;
}

.ranking-table th {
  background-color: #f8f9fa;
  color: #555;
  font-weight: 600;
}

.ranking-table tr:last-child td {
  border-bottom: none;
}

.rank-col {
  width: 50px;
  font-weight: bold;
  color: #588317;
}

.player-col {
  font-weight: 500;
}

.wins-col {
  text-align: right;
  font-weight: bold;
}

.empty-msg {
  text-align: center;
  color: #999;
  font-style: italic;
}
</style>

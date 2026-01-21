<script setup>
import { ref, shallowRef, onMounted, onUnmounted, watch } from 'vue';
import { restoreGame, verifyMove, getBoardState, applyMove } from '../../assets/teavm/checkers-engine.mjs';

const emit = defineEmits(['send-move', 'disconnect-from-game', 'board-state']);

const props = defineProps(['move', 'moveHistory', 'myColor']);

const warcaby = shallowRef(null);
const myTurn = ref(true);
const moveOk = ref(true);
const moveInput = ref('');

watch(() => props.move, () => {
  const result = verifyMove(warcaby.value, props.move);
  applyMove(warcaby.value);
  console.log('Move applied, result:', result);
  myTurn.value = !(props.myColor === result);
  emit('board-state', getBoardState(warcaby.value));
});

onMounted(() => {
  console.log(props.moveHistory);
  const currentPlayer = props.moveHistory ? props.moveHistory.length % 2 === 0 ? 'Biały' : 'Czarny' : 'Biały';
  myTurn.value = (props.myColor === currentPlayer);
  warcaby.value = restoreGame(props.moveHistory || []);
  emit('board-state', getBoardState(warcaby.value));
});

onUnmounted(() => {
  warcaby.value = null;
  emit('disconnect-from-game');
});

const checkMove = (moveStr) => {
  try {
    if (moveStr.trim()) {
      verifyMove(warcaby.value, moveStr);
      moveOk.value = true;
      return true;
    }
  } catch (err) {
    moveOk.value = false;
    return false;
  }
};

watch(moveInput, (newVal) => {
  checkMove(newVal);
});

const sendMove = () => {
  if (checkMove(moveInput.value)) {
    emit('send-move', moveInput.value);
    moveInput.value = '';
  }
};
</script>

<template>
  <div class="controls-card">
    <div class="input-group">
      <input 
        v-model="moveInput" 
        type="text" 
        :disabled="!myTurn"
        :placeholder="myTurn ? 'Ruch (np. A3 B4)' : 'Zaczekaj na ruch przeciwnika'" 
        @keyup.enter="sendMove"
        :class="{ error: !moveOk }"
      />
      <button @click="sendMove" :disabled="!moveOk || !myTurn">Wyślij</button>
    </div>
  </div>
</template>

<style scoped>
.controls-card {
  width: 100%;
  padding: 1.5rem;
  border-radius: 12px;
  background-color: #ffffff;
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
  gap: 1rem;
  box-sizing: border-box; /* Ensure padding doesn't affect width */
}

.input-group {
  display: flex;
  gap: 10px;
  width: 100%;
}

@media (max-width: 600px) {
  .controls-card {
    padding: 1rem;
    margin-top: 10px;
  }
  
  .input-group {
    flex-direction: column;
  }

  button {
    width: 100%;
  }
}

input {
  flex: 1;
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

input.error {
  border-color: #c62828;
}

button {
  padding: 0.75rem 1.5rem;
  background-color: #588317; /* var(--dark-green) */
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.2s, transform 0.1s;
  white-space: nowrap;
}

button:disabled {
  background-color: #a5a5a5;
  cursor: not-allowed;
}

button:disabled:hover {
  background-color: #c62828;
}

button:hover {
  background-color: #4a6e13;
}

button:active {
  transform: scale(0.98);
}
</style>

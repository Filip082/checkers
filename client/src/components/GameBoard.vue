<script setup>
import { computed } from 'vue';

const props = defineProps(['pieces', 'myColor']);

// Helper to calculate coordinate ID from index (0-63)
// Index 0 = Top-Left (A8)
// Index 63 = Bottom-Right (H1)
const getCoordinate = (index) => {
  const row = Math.floor(index / 8);
  const col = index % 8;
  
  const rank = 8 - row; // 0 -> 8, 7 -> 1
  const file = String.fromCharCode(65 + col); // 0 -> A, 7 -> H
  
  return `${file}${rank}`;
};

const cols = ['A','B','C','D','E','F','G','H'];
const rows = [8,7,6,5,4,3,2,1];

const isDark = (index) => {
  const row = Math.floor(index / 8);
  const col = index % 8;
  return (row + col) % 2 === 1;
};
</script>

<template>
  <div class="board-container">
    <!-- Top Labels (A-H) -->
    <div class="board-labels top">
      <span v-for="l in (props.myColor === 'Biały' ? cols : cols.slice().reverse())" 
      :key="'t'+l">{{ l }}</span>
    </div>

    <div class="board-row">
      <!-- Left Labels (8-1) -->
      <div class="board-labels left">
        <span v-for="n in (props.myColor === 'Biały' ? rows : rows.slice().reverse())" :key="'l'+n">{{ n }}</span>
      </div>

      <!-- The Board -->
      <div class="board">
        <div 
          v-for="(foo, index) in 64" 
          :key="index" 
          class="square" 
          :class="{ dark: isDark(props.myColor === 'Biały' ? index : 63 - index), light: !isDark(props.myColor === 'Biały' ? index : 63 - index) }"
        >
          <!-- Piece Rendering -->
          <div 
            v-if="props.pieces[getCoordinate(props.myColor === 'Biały' ? index : 63 - index)]" 
            class="piece" 
            :class="props.pieces[getCoordinate(props.myColor === 'Biały' ? index : 63 - index)]"
          ></div>
        </div>
      </div>

      <!-- Right Labels (8-1) -->
      <div class="board-labels right">
        <span v-for="n in (props.myColor === 'Biały' ? rows : rows.slice().reverse())" :key="'r'+n">{{ n }}</span>
      </div>
    </div>

    <!-- Bottom Labels (A-H) -->
    <div class="board-labels bottom">
      <span v-for="l in (props.myColor === 'Biały' ? cols : cols.slice().reverse())" :key="'b'+l">{{ l }}</span>
    </div>
  </div>
</template>

<style scoped>
/* Colors */
.light {
  background-color: var(--board-light);
}
.dark {
  background-color: var(--board-dark);
}

.board-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.board-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.board {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  grid-template-rows: repeat(8, 1fr);
  gap: 0;
  width: min(72vmin, 60vh, 680px);
  aspect-ratio: 1 / 1;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 8px 18px rgba(16,24,40,0.08);
  /* border: 4px solid #5d4037; */
}

.square {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

/* PIECE STYLES */
.piece {
  width: 75%;
  height: 75%;
  border-radius: 50%;
  box-shadow: 0 4px 6px rgba(0,0,0,0.4), inset -3px -3px 5px rgba(0,0,0,0.2), inset 3px 3px 5px rgba(255,255,255,0.3);
  transition: transform 0.2s;
}

.piece:hover {
  transform: scale(1.05);
  cursor: pointer;
}

/* White / Light Piece */
.piece.white {
  background-color: #f0f0f0;
  border: 2px solid #ccc;
}

/* Burgundy / Red Piece */
.piece.red {
  background-color: #800020; /* Burgundy */
  border: 2px solid #500014;
}

/* Labels */
.board-labels.top,
.board-labels.bottom {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  width: min(72vmin, 60vh, 680px);
  text-align: center;
  font-weight: 600;
  color: #ddd;
  font-size: 0.9rem;
  margin: 5px 0;
}

.board-labels.left,
.board-labels.right {
  display: grid;
  grid-template-rows: repeat(8, 1fr);
  height: min(72vmin, 60vh, 680px);
  align-items: center;
  justify-items: center;
  font-weight: 600;
  color: #ddd;
  font-size: 0.9rem;
  margin: 0 10px;
}

.board-labels span {
  user-select: none;
}
</style>

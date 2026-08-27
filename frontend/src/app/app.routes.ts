import { Routes } from '@angular/router';
import { MainScreenComponent } from './pages/main-screen/main-screen.component';
import { GameScreenComponent } from './pages/game-screen/game-screen.component';

export const routes: Routes = [
  { path: '', component: MainScreenComponent },
  { path: 'game', component: GameScreenComponent },
  { path: '**', redirectTo: '' },
];

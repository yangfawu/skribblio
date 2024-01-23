import { NgModule } from '@angular/core';
import { AngularFireModule } from '@angular/fire';
import { AngularFireAuthModule } from "@angular/fire/auth";
import { AngularFireDatabaseModule } from "@angular/fire/database";
import { BrowserModule } from '@angular/platform-browser';
import { environment } from 'src/environments/environment';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './components/app/app.component';
import { AvatarComponent } from './components/avatar/avatar.component';
import { NgxWebstorageModule } from 'ngx-webstorage';
import { HomeComponent } from './components/home/home.component';
import { RoomComponent } from './components/room/room.component';
import { LoadingComponent } from './components/loading/loading.component';
import { GameComponent } from './components/game/game.component';


@NgModule({
    declarations: [
        AppComponent,
        AvatarComponent,
        HomeComponent,
        RoomComponent,
        LoadingComponent,
        GameComponent
    ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        NgxWebstorageModule.forRoot(),
        AngularFireModule.initializeApp(environment.firebaseConfig),
        AngularFireAuthModule,
        AngularFireDatabaseModule
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule { }

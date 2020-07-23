# Alite 2020
by Philipp Bouillon and Duane McDonnell

### About

Alite is a fan port of the 1980s game Elite, made for Android devices. For more information, visit http://alite.mobi and https://alite2020.iftopic.com or download the release from the PlayStore:

https://play.google.com/store/apps/details?id=de.phbouillon.android.games.alite

Command your Cobra space ship in a fantastic voyage of discovery, adventure,
and trade, a supreme test of your combat, navigational and entrepreneurial
skills.

Designed specifically for Android devices, Alite brings space action and
exploration directly to you - wherever you are.

Alite is inspired by classic Elite, (c) Acornsoft, Bell & Braben.

### Version History
* Version 1.6.0 07/23/2020
  * Achievements feature is added
  * Extended galaxies added
  * Stars as lines at torus speed
  * Vibrate for on hit added
  * Assisted docking added
  * Continuous / tap tutorial mode added
  * Equipments extracted to equipment.plist

* Version 1.5.9 05/11/2019
  * THIS VERSION IS NOT YET PULLED FROM PHILIPP REPOSITORY, IT IS CURRENTLY THE VERSION PLACEHOLDER ONLY. Improvement: Beginning of a controller implementation that allows the complete control of Alite via a gamepad. The general architecture is now present. The tedious bit is now to implement the controls for each and every screen. For now, the "simple ones" work: Intro-Movie (skip by pressing any button), ShipIntroScreen (change the ships by pressing left/right on the dpad or by using the primary stick, acknowledge the tap to start button by pushing A, or changing between the Load New Commander Yes/No options by using the second stick), Navigation Bar (secondary stick up/down, B to select), dialogs (secondary stick left/right, A to select -- does not work for all modal dialogs, yet).
</br>

* Version 1.5.8 05/11/2019
  * Change: Activated the ship debug mode. If that bothers you, change DEBUG_EXHAUST and ONLY_CHANGE_SHIPS_AFTER_SWEEP to false in the ShipIntroScreen.  
  * Bugfix: Missiles, fuel, and lasers would be lost if you equipped them and immediately pressed "Home".
  * Bugfix: Fixed broken textures: Some textures used a 64bit length color coding… 
</br>

* Version 1.5.7	09/17/2016
  * Bugfix: When pressing "Home", it might have happened that the old Commander was replaced by a new one (introduced in 1.5.6) this is now fixed.
</br>

* Version 1.5.6	09/17/2016
  * Improvement: If searching for a planet outside the current galaxy, the number of the galaxy the planet is in will be displayed.
  * Bugfix: If the cobra is spinning endlessly in space, the state is now reset after 10s.
  * Bugfix: If leaving Alite by pressing 'Home' the game state is now saved again.
</br>

* Version 1.5.5	08/28/2016
  * Bugfix: Launches the missiles from below the ship so that they do not cross the laser fire immediately.
  * Bugfix: Small bugfix for multi user phones.
</br>
          
* Version 1.5.4	08/18/2016
  * Bugfix: FINALLY fixes the accelerometer controls. More often than not, they were broken after Alite was resumed, or when you play the tutorial. No more.
  * Bugfix: Also, some devices crashed when playing back music. This has also been fixed.
</br>

* Version 1.5.3	08/05/2016
  * Improvement: Lets you toggle full immersion mode in the Options; note that Alite needs to be restarted for the changes to take effect.
</br>

* Version 1.5.2	08/02/2016
  * Bugfix: full immersion mode recenters the screen again. Only available on the PlayStore.
</br>

* Version 1.5.1	07/30/2016
  * Improvement: Gives you full immersion mode, which means nothing on most devices, but will hide the annoying lights of the navigation bar on some tablets. Only available on the PlayStore.
</br>

* Version 1.5.0	05/08/2016
  * Change: Has a new legal system: Vipers won't attack the player in unsafer systems.
  * Bugfix: Fixed a corrupt texture.
  * Bugfix: Improved the commander save routines.
</br>

* Version 1.4.8	04/03/2016
  * Improvement: difficulty level feature added, so that you can reduce the number of enemies if the game is too hard in the beginning.
  * Change: Brings back the old accelerometer control in addition to the new one, as the new one has problems on some devices (for example the Nexus 10).
</br>

* Version 1.4.7	03/13/2016
  * Bugfix: Works around a bug in Android 5.0 -- and some versions above (https://code.google.com/p/android/issues/detail?id=81187).
  Loading of small sound files is very slow, since this is a bug in Android, all Alite can do to reduce its effect is to put loading of sounds in the background. While on most Android versions, loading of all sounds takes about 2s, on "bad" versions, it takes up to 2 minutes. So, on Android 5, it can happen that there should be a sound but it isn't played until it is finally loaded.
</br>

* Version 1.4.6	03/03/2016
  * Change: Also changed the radar color of cargo canisters.
  * Bugfix: Really fixes the accelerometer control. It's now independent of the orientation.
</br>

* Version 1.4.5	02/28/2016
  * Bugfix: Tries to fix a problem with the accelerometer control: If you are playing lying down, the pitch will change in mid-movement.
</br>

* Version 1.4.4	02/12/2016
  * Bugfix: Repairs the broken 1.4.3, which contained a typo and thus rendered the version useless.
</br>

* Version 1.4.3	02/11/2016
  * Bugfix: Fixes a bug in mission 4, which causes an exception when entering hyperspace after receiving this mission.
</br>

* Version 1.4.2	02/07/2016
  * Improvement: Comes with an improved "Medium Speed" Docking Computer.
  * Bugfix: The algorithm now correctly evades other ships (and the space station), without crashing into them.
  * Bugfix: Another bug regarding pausing the game when explosions were visible, was fixed.
</br>

* Version 1.4.1	01/23/2016
  * Improvement: Medium Speed Docking Computer added.
  * Bugfix: Corrupt image replaced.
  * Bugfix: Planet Info in WitchSpace enabled.
  * Bugfix: Asteroid course randomized.
  * Bugfix: Tutorials now work if the legal status is not clean.
</br>

* Version 1.4.0	01/10/2016
  * Improvement: Introduces the TimeWarp feature, implemented by Steven Phillips. The TimeWarp lets you pass those regions in space where the torus drive is not an option.
  * Improvement: Add eject cargo feature, and does not punish the player for scooping up contraband goods anymore.
  * Improvement: You can now also alter the sound volume for different sets of effects.
  * Improvement: Configurable vibration effect was added.
  * Bugfix: Some bugfixes were made.
</br>

* Version 1.3.4	12/21/2015
  * Bugfix: Fixes a bug in the Galaxy and Local Screens. The background was not cleared correctly on some devices.
</br>

* Version 1.3.3	12/15/2015
  * Bugfix: Fixes a bug in the "Trading" and "Basic Flying" tutorials, which was introduced in version 1.3.0.
</br>

* Version 1.3.2	12/12/2015
  * Bugfix: Fixes a bug in the Equipment screen: If you had turned off animations (via Options/Display Options), the game would crash.
</br>

* Version 1.3.1	12/06/2015
  * Improvement: Introduces the display of expected gain (or loss) from a trade
  * Improvement: The "flat button layout" (detailed here: http://alite.mobi/node/30).
  * Improvement: You can set the laser firing to single shot and can still change speed while firing.
  * Bugfix: For screens larger than 1920x1080, library and disk screens now work, too.
  * Bugfix: Some sound effects have been fixed (most notably, the torus drive sound stops playing when entering hyperspace).
</br>

* Version 1.3.0	12/01/2015
  * Improvement: Lets you invert the controls
  * Bugfix: fixes a small overlay bug in the Cursor keys.
</br>

* Version 1.2.1	10/28/2015
  * Bugfix: Turns the cheat mode back off :). Ooops... I left it in acidentally in version 1.2.0.
</br>

* Version 1.2.0	10/25/2015
  * Bugfix: Finally fixes the vanishing objects bug and works on Nexus 9/10 devices.
</br>

* Version 1.1.2	09/12/2015
  * Bugfix: Boomslang rename was buggy. Now the Bushmaster is really called "Harken".
  * Structural: First Source code release.
</br>

* Version 1.1.1	09/06/2015
  * Bugfix: The application doesn't crash anymore when its state is restored (bug introduced with version 1.1).
  * Bugfix: Lasers now correctly vanish when they hit an object.
  * Bugfix: Button images in the ShipIntroScreen are scaled again (another bug introduced with version 1.1).
</br>

* Version 1.1	08/30/2015
  * Improvement: Comes with Chris Huelsbeck's rendition of the Blue Danube.
  * Change: Two ships have been renamed to "Harken" and "Lancehead", respectively.
</br>

* Version 1.0	08/05/2015
  * Structural: First release of Alite.


## Build instructions

Alite has been developed with ADT, I have never tried to set it up in Android
Studio, but it should not be too hard.

If you import Alite into ADT and connect your device, you can install it
without the need to download any additional libraries. However, there are a
couple of things you need to know:

In the class "de.phbouillon.android.games.alite.AliteConfig", you find
the constant HAS_EXTENSION_APK, which is set to true. This means that all
assets of Alite are stored in an "obb" file, which needs to be installed in
the directory "<phone>/Android/obb/de.phbouillon.android.games.alite/main.2192.de.phbouillon.android.games.alite.obb".

Now you have two options:

#### Option 1: Use an extension file
(Pro: Fast testing of code changes in Alite, deployment is fast,
 Con: Devilishly complicated to create an obb file, need to create
      a new obb file for each change in assets (textures/music/...))

You can either create this file (see below) or use the existing obb file
from the PlayStore version of Alite; if you downloaded Alite from the
PlayStore, the obb file will already be on your phone. In that case, copy
it to your computer, so that you have a backup.

Note that if you deploy Alite from your Computer, it has to uninstall the
official Alite version on your phone, because it uses a different key store,
so the obb file on your phone _will be deleted_ when you first install
Alite from your computer. You will have to copy the obb file back from
your computer afterwards.

If you choose to create the obb file yourself, please download the jobb tool
from this location:
https://www.dropbox.com/sh/8xk89ekj1138waq/AACit-2wkFZIz55ZFcO6XrtIa
Copy _both files_ (jobb.jar and fat32lib.jar) to a directory on your drive.
Then copy the Resources folder from Alite to that directory as well. Open
a command prompt or shell in that directory and type:

java -jar jobb.jar -d Resources -o main.<VERSION>.de.phbouillon.android.games.alite.obb -pn de.phbouillon.android.games.alite -pv <VERSION>

and make sure that <VERSION> _exactly!_ matches the version number you
specified in the AliteConfig, see EXTENSION_FILE_VERSION.

Once built set EXTENSION_FILE_LENGTH in AliteConfig to the size of your
new obb file in bytes and rebuild & install Alite.

Why is that so complicated?! I wish I knew :). The thing is that the jobb tool
is pretty buggy and if you really must have an obb file, this version of the
jobb tool seems to be the one which makes most devices happy. Still: Some
devices absolutely cannot read the obb file, and for those, there is the
"all-in-one-solution".

If you want more information about extension files, I suggest you read this
article here: http://developer.android.com/google/play/expansion-files.html
(but be wary: It's a slippery road :))

#### Option 2: Do not use an extension file
(Pro: Easy to set-up,
 Con: Slow deployment. It takes several minutes to deploy a new Alite version;
      if you're making many small changes to the code, that can be annoying)

If you only want to enhance Alite and test it on your device, or you don't
want to bother handling OBBs, set the "HAS_EXTENSION_APK" flag to false in
the AliteConfig class.
This tells Alite that all files have to be loaded from the assets directory.
So, if you set the flag to "false", copy all files from the Resources/assets
directory to the assets/ directory.
Next, copy the files from the Resources/intro directory to the res/raw
directory ("raw" has to be created).
Now, one more thing to do:
In "AliteConfig", you'll find the constants ALITE_INTRO_XXX. Replace their
-1 value with the "R.raw.alite_intro_xxx" value you'll find in the comments
of the AliteConfig file.

Done. When you now deploy Alite, you don't have to keep an OBB file on your
phone and you'll instantly see changes to resources you made. But deployment
will take longer...

I hope this allows you to deploy Alite from your computer... If you have any
questions, please post them at the forum over at http://alite.mobi/forum.

Good luck -- and have fun!
Cmdr Stardust aka Philipp Bouillon

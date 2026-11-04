# Restore PlaybackEngine.kt + apply CC fix

`PlaybackEngine.kt` on `master` was temporarily corrupted during an automated push.
Run these commands to restore from upstream and apply the caption-safety fix:

```bash
git clone https://github.com/sheikhdipuraihan-sudo/Litube.git
cd Litube
git fetch https://github.com/HydeYYHH/Litube.git master
git checkout master

# Restore the full file from upstream HydeYYHH/Litube
git show FETCH_HEAD:app/src/main/java/com/hhst/youtubelite/player/engine/PlaybackEngine.kt \
  > app/src/main/java/com/hhst/youtubelite/player/engine/PlaybackEngine.kt

# Apply the CC "could not play" fix
git apply fix-cc-playback-fallback.patch

git add app/src/main/java/com/hhst/youtubelite/player/engine/PlaybackEngine.kt
git commit -m "fix(player): keep playback alive when enabling captions fails"
git push origin master
```

## What the fix does

When you turn on CC, Litube rebuilds the media source to attach subtitles.
If that subtitle URL fails (403 / expired timedtext / network), the player
used to die with "could not play this video".

Now it retries once **without** subtitles so the video keeps playing.

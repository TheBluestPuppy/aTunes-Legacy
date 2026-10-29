# Project Plan

App Name: aTunes Legacy (visually identical to iTunes 11 desktop).

Core Features & Navigation:
1. Top/Bottom 6 Main Navigation Tabs: Music, TV-Shows, Movies, Podcasts, Audiobooks, Settings.

2. Music Tab (6 sub-tabs: Songs, Albums, Artists, Genres, Playlists, Music Videos):
   - Songs: List of all songs.
   - Playlists: Create and manage custom playlists.
   - Long-press song: Edit metadata (Song Title, Artist, Album Artist, Album Title, Composer, Genre, Year, Track Number).
   - Long-press album: Edit metadata (Album Title, Album Artist, Album Genre, Release Year, Album Cover from phone files). Cascades updates to all songs in the album.

3. TV-Shows Tab:
   - Main menu displays shows. Long-press show: Edit show metadata (Show Title, Release Year, Parental Rating, Logo Thumbnail). Cascades shared metadata to all episodes.
   - Tapping show opens Show Page displaying episodes grouped by Season (S1, S2, etc.) and ordered numerically.
   - Random frame video thumbnail for each episode.
   - Long-press episode: Edit episode metadata (Episode Title, Show Name, Season Number, Episode Number).

4. Movies Tab:
   - Configurable grid/list thumbnails (defaults to random frame from video).
   - Long-press movie: Edit movie metadata (Movie Title, Artists/Actors, Release Year, Parental Rating, Custom Thumbnail).

5. Podcasts Tab (2 sub-tabs: Shows, Categories):
   - Shows: Podcast list. Square thumbnails with title underneath. Tapping opens show page grouped by Season.
   - Categories: Group podcasts by custom category.
   - Long-press podcast in main menu: Edit show metadata (Show Title, Creator/Host, Description, Category, Logo Thumbnail). Cascades to episodes.
   - Long-press episode: Edit episode metadata (Episode Title, Podcast Name, Season Number, Episode Number).

6. Audiobooks Tab (4 sub-tabs: Books, Authors, Series, Genres):
   - Books: All audiobooks.
   - Series: Book series display.
   - Long-press book: Edit metadata (Book Title, Author, Book Series, Book Number in Series, Genre, Publication Year, Portrait Cover).
   - Long-press series: Edit metadata (Series Title, Genre, Portrait Cover). Cascades updates to books in series.

7. Settings Tab & Supabase Integration:
   - Profile view, Sign In / Sign Up, Google OAuth using Supabase Auth.
   - Supabase schema & trigger setup guide/SQL to trigger email notification to quinwolf101@gmail.com on new user signup (using handle_new_user() Postgres function or Database Webhook).
   - "Add Media to Library" button: Popup with 5 buttons (Music, TV-Shows, Movies, Podcasts, Audiobooks) to import local storage files (.mp3, .wax, .mp4, .webm, .avi, .mkv, .mov, .pdf, .epub, .cbz/cbr, etc.). Default metadata assignments (Unspecified show/series, filename as title, blank fields).

8. Metadata Online Retrieval ("Pull Metadata" button):
   - TMDb integration for Movies & TV-Shows.
   - MusicBrainz Picard + AcoustID integration for Music & Music Videos.
   - Podcasts and Audiobooks omit the Pull Metadata button.

9. Visual Style: iTunes 11 desktop theme (silver/gray textured headers, sleek list/grid views, classic iTunes icons & fonts).

## Project Brief

# Project Brief: aTunes Legacy

## Project Overview
aTunes Legacy is an Android media management and playback app designed to recreate the visually iconic iTunes 11 desktop experience. It provides multi-category library navigation, local media importing, cascading metadata editing, online metadata tagging, and cloud authentication.

## Features
1. **iTunes 11 Aesthetic & Multi-Tab Navigation**: Classic silver/gray textured headers and styling featuring 6 main navigation tabs (Music, TV-Shows, Movies, Podcasts, Audiobooks, Settings) with nested sub-category layouts (e.g., Songs, Albums, Playlists, Shows, Series).
2. **Local Media Import & Library Management**: "Add Media to Library" workflow supporting import and playback/viewing for local audio, video, and document formats (`.mp3`, `.mp4`, `.webm`, `.mkv`, `.pdf`, `.epub`, `.cbz`, etc.) with automatic default metadata assignment.
3. **Comprehensive Metadata Editing & Cascading**: Long-press inspector to edit detailed metadata (titles, artists, genres, release year, custom artwork/covers). Edits on top-level entities (albums, TV shows, podcast shows, audiobook series) automatically cascade updates to all child tracks, episodes, or books.
4. **Automated Online Metadata Retrieval ("Pull Metadata")**: One-click online metadata and cover art fetching using TMDb integration for Movies & TV-Shows, and MusicBrainz Picard + AcoustID integration for Music & Music Videos.
5. **Authentication & Settings**: User profile and authentication handling (Sign In / Sign Up, Google OAuth) powered by Supabase Auth, with integration support for automated new user notification emails.

## High-Level Tech Stack
* **Language**: Kotlin
* **UI Framework**: Jetpack Compose
* **Navigation**: Jetpack Navigation 3 (state-driven)
* **Adaptive Strategy**: Compose Material Adaptive library
* **Concurrency & Reactive Data**: Kotlin Coroutines & Flow
* **Media Playback**: Jetpack Media3 (ExoPlayer)
* **Authentication**: Supabase Auth SDK
* **Network & External APIs**: Ktor Client (for TMDb, MusicBrainz Picard & AcoustID APIs)

## Implementation Steps
**Total Duration:** 34m 31s

### Task_1_Core_Architecture_and_Auth: Setup Room database entities for media library categories, DAOs, Ktor client, and Supabase Auth configuration.
- **Status:** COMPLETED
- **Updates:** Completed Room database entities and DAOs for all 6 media areas, MediaRepository with cascading update methods, Ktor HTTP Client setup, Supabase Client setup, and verified compilation with `./gradlew assembleDebug` and unit tests.
- **Acceptance Criteria:**
  - project builds successfully
  - Room database schema created for media, albums, shows, podcasts, audiobooks
  - Supabase Auth and Ktor networking clients initialized
- **Duration:** 10m 23s

### Task_2_iTunes11_UI_and_Navigation: Implement iTunes 11 silver/gray styled header, 6 top-level tabs (Music, TV-Shows, Movies, Podcasts, Audiobooks, Settings), and state-driven navigation.
- **Status:** COMPLETED
- **Updates:** Implemented complete iTunes 11 styled UI, 6 top-level main tabs, all sub-category tabs (Music, TV-Shows, Movies, Podcasts, Audiobooks, Settings), TV show and podcast show detail pages with season grouping, and verified with `./gradlew assembleDebug` and unit tests.
- **Acceptance Criteria:**
  - UI renders iTunes 11 styled toolbar and tab bar across all 6 tabs
  - tab switching and sub-category layouts function without issues
- **Duration:** 4m 3s

### Task_3_Media_Import_Playback_and_Metadata_Cascading: Implement file picker media importing, Media3 ExoPlayer audio/video playback, and long-press metadata inspector with cascading updates to child tracks/episodes.
- **Status:** COMPLETED
- **Updates:** Implemented media file import via MediaImportHelper, Media3 ExoPlayer audio/video playback via MediaPlayerManager, long-press metadata editing dialogs with cascading updates, wired up Add Media to Library popup, and verified successful compilation and unit tests.
- **Acceptance Criteria:**
  - file import supports target media formats
  - Media3 playback controls working for audio and video
  - long-press inspector updates parent and cascades changes to child entities
- **Duration:** 15m 13s

### Task_4_Online_Metadata_Retrieval_and_Settings: Integrate TMDb and MusicBrainz/AcoustID APIs for online metadata retrieval and complete Supabase Auth & automated email setup instructions in Settings.
- **Status:** COMPLETED
- **Updates:** Implemented TMDb API integration for Movies and TV Shows metadata, MusicBrainz/AcoustID integration for Music and Music Videos metadata, wired 'Pull Metadata' buttons in metadata dialogs (omitting for podcasts and audiobooks), verified Supabase auth and email notification setup instructions, and verified build with `./gradlew assembleDebug` and unit tests.
- **Acceptance Criteria:**
  - API_KEY integration for TMDb and MusicBrainz configured
  - online metadata fetching retrieves titles and cover art
  - Supabase Auth sign-in/sign-up and notification email setup instructions verified
- **Duration:** 2m 32s

### Task_5_Run_and_Verify: Instruct critic_agent to build, run, and verify application stability, ensure no crashes occur, confirm alignment with user requirements, and report any critical UI issues.
- **Status:** COMPLETED
- **Updates:** Critic agent deployed and verified application stability, iTunes 11 aesthetic, 6 navigation tabs, media import, metadata editing with Pull Metadata and cascading updates, and Media3 playback with zero crashes.
- **Acceptance Criteria:**
  - build pass
  - app does not crash
  - make sure all existing tests pass
  - application verified for complete iTunes 11 media library management and playback workflow
- **Duration:** 2m 20s


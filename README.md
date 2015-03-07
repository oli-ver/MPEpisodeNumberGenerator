# ENGLISH
## MPEpisodeNumberGenerator
Series and episode number generator for Mediaportal 1.X

This application scans the EPG data of your MediaPortal MySql database and adds series and episode numbers to all episodes in EPG. Other applications with this features only update the epg data of series that have scheduled recordings set in MediaPortal.

If you start this application before running TV Movie EPG import (Clickfinder) and TvWishList there will be no recordings of seasons that you do not need anymore.

Please rename the template files in the config directory deleting the suffix _template and configure them to match your needs. The application will not start until you do. Do not change the value of the proxy. This is only a small application running on my server to make it possible not to provide my thetvdb.com api key to the users.

The pre-release MPEpisodeNumberGenerator 1.0 was tested using the following configuration:

## MediaPortal configuration
MediaPortal 1.10 TVServer application
mysql  Ver 14.14 Distrib 5.6.10, for Win64 (x86_64)

Installed plugins:
PowerScheduler (not relevant for MPEpisodeNumberGenerator)
TV Movie EPG import++
TvWishList

The application does not support Microsoft SQL Server Express Edition at the moment.

## Installation

1. Download the Binaries here: https://github.com/oli-ver/MPEpisodeNumberGenerator/releases/download/1.0/MPEpisodeNumberGenerator.zip
2. Unpack the application in a directory where you have both read and write permissions
3. Rename or copy the template files in the config directory deleting the suffix "_template" in the filename and configure them to match your needs
4. Start the application using the cmd script

# Deutsch
## MPEpisodeNumberGenerator
Serien- und Episoden-Nummer Generator für Mediaportal 1.X

Diese Applikation scannt die EPG-Daten einer MediaPortal MySql-Datenbank und fügt die Staffel- und Folgen-Nummern zu allen Folgen im EPG hinzu. Andere Applikationen mit diesen Funktionen aktualisieren die EPG-Daten nur für die Serien, zu denen Folgen zur Aufnahme in MediaPortal programmiert worden sind.

Wenn diese Applikation vor dem Start von TV Movie EPG import (Clickfinder) und TvWishList gestartet wird, werden keine Aufnahmen von TvWishlist mehr programmiert, die nicht benötigt werden.

Bitte benennt die Template-Dateien im config-Verzeichnis um, indem das Suffix "_template" gelöscht wird und konfiguriert sie nach eurer Umgebung. Die Applikation wird nicht starten, bis ihr das tut. Ändert nicht den Wert des Proxys. Es handelt sich nur um eine kleine Applikation auf meinem Server, um es zu ermöglichen den thetvdb.com API Schlüssel nicht an die Benutzer auszuliefern.

Dieses Pre-Release MPEpisodeNumberGenerator 1.0 wurde mit folgender Konfiguration getestet:

## MediaPortal Konfiguration
MediaPortal 1.10 TVServer Applikation
mysql  Ver 14.14 Distrib 5.6.10, for Win64 (x86_64)

Installierte Plugins:
PowerScheduler (nicht relevant für MPEpisodeNumberGenerator)
TV Movie EPG import++
TvWishList

Das Programm unterstützt im Moment nicht Microsoft SQL Server Express Edition.

## Installation

1. Ladet euch die Binaries hier herunter: https://github.com/oli-ver/MPEpisodeNumberGenerator/releases/download/1.0/MPEpisodeNumberGenerator.zip
2. Entpackt die Applikation in ein Verzeichnis, in dem  ihr Lese- und Schreib-Berechtigung habt
3. Kopiert die Template-Dateien im Config-Verzeichnis oder benennt sie um, wobei ihr das Suffix "_template" im Dateinamen entfernen müsst. Konfiguriert sie anschließend, so dass sie auf eure Umgebung passen.
4. Startet die Applikation mit dem cmd Skript
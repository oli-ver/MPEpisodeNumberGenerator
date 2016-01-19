# ENGLISH
## MPEpisodeNumberGenerator
Series and episode number generator for Mediaportal 1.X

This application scans the EPG data of your MediaPortal MySql database and adds series and episode numbers to all episodes in EPG. Other applications with this features only update the epg data of series that have scheduled recordings set in MediaPortal.

If you start this application after running TV Movie EPG import (Clickfinder) and TvWishList there will be no recordings of seasons that you do not need anymore.

Please rename both template files in the config directory deleting the suffix _template and configure them to match your needs. The application will not start until you do. Do not change the value of the proxy. This is only a small application running on my server to make it possible not to provide my thetvdb.com api key to the users.

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

1. Download the Binaries here: https://github.com/oli-ver/MPEpisodeNumberGenerator/releases/download/1.0.1/MPEpisodeNumberGenerator-1.0.1.zip
2. Unpack the application in a directory where you have both read and write permissions
3. Rename or copy the template files in the config directory deleting the suffix "_template" in the filename and configure them to match your needs. You have to do this, or the application will not start.
4. Change the config parameters so it will match to your TV server's configuration. The following parameters are mandatory and have to be changed: mysqldatabasepath, mediaportaldbuser, mediaportaldbpassword, mediaportaldbhost, mediaportaldbname.
5. Edit the start script and add the following line:
cd "path\to\your\MPEpisodeNumberGenerator"
6. Start the application using the cmd script

## Configuration

The following configuration options can be set in settings.properties.

#Path to local mysql database
mysqldatabasepath=C\:\\Program Files\\MySQL\\MySQL Server 5.6\\bin\\

#Timestamp of last run
lastrun=2015-02-28 14\:30\:21

#EPG indicator that indicates that the program is a series and no film (The german EPG begins with "Folge:" if you use TV Movie Clickfinder)
epgdescriptionseriesindicator=Folge\:

#EPG description pattern to find series and episode number from description text if not known by thetvdb.com
epgdescriptionpattern=Dies ist die \\d{1,}\\. Episode der \\d{1,}\\. Staffel.*

#MediaPortal database user
mediaportaldbuser=root

#top level qualifier of your country (to chose a mirror from thetvdb)
tld=de

#language of your epg data
language=de

# your database password
mediaportaldbpassword=

# your database backup part (a dump is created before processing the epg)
backuppath=bak/

# Your database host
mediaportaldbhost=localhost

# Your database name
mediaportaldbname=mptvdb

# Thetvdb api proxy url
thetvdb.proxy=http://science-site.de:9000/thetvdb/

# Offline mode
offline=true

# Deutsch
## MPEpisodeNumberGenerator
Serien- und Episoden-Nummer Generator für Mediaportal 1.X

Diese Applikation scannt die EPG-Daten einer MediaPortal MySql-Datenbank und fügt die Staffel- und Folgen-Nummern zu allen Folgen im EPG hinzu. Andere Applikationen mit diesen Funktionen aktualisieren die EPG-Daten nur für die Serien, zu denen Folgen zur Aufnahme in MediaPortal programmiert worden sind.

Wenn diese Applikation nach dem Start von TV Movie EPG import (Clickfinder) und TvWishList gestartet wird, werden keine Aufnahmen von TvWishlist mehr programmiert, die nicht benötigt werden.

Bitte benennt beide Template-Dateien im config-Verzeichnis um, indem das Suffix "_template" gelöscht wird und konfiguriert sie nach eurer Umgebung. Die Applikation wird nicht starten, bis ihr das tut. Ändert nicht den Wert des Proxys. Es handelt sich nur um eine kleine Applikation auf meinem Server, um es zu ermöglichen den thetvdb.com API Schlüssel nicht an die Benutzer auszuliefern.

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

1. Ladet euch die Binaries hier herunter: https://github.com/oli-ver/MPEpisodeNumberGenerator/releases/download/1.0.1/MPEpisodeNumberGenerator-1.0.1.zip
2. Entpackt die Applikation in ein Verzeichnis, in dem  ihr Lese- und Schreib-Berechtigung habt
3. Kopiert beide Template-Dateien im Config-Verzeichnis oder benennt sie um, wobei ihr das Suffix "_template" im Dateinamen entfernen müsst. Das muss zwingend gemacht werden, sonst startet die Applikation nicht
4. Konfiguriert sie anschließend, so dass sie auf eure Umgebung passen. Folgende Parameter müssen zwingend geändert werden, sonst kann das Programm nicht laufen: mysqldatabasepath, mediaportaldbuser, mediaportaldbpassword, mediaportaldbhost, mediaportaldbname.
5. Bearbeitet das Start-Skript und fügt folgende Zeile hinzu:
cd "path\to\your\MPEpisodeNumberGenerator"
6. Startet die Applikation mit dem cmd Skript

## Konfiguration

Die folgenden Parameter können in der Datei settings.properties eingestellt werden:

#Pfad zur lokalen MySQL-Installation
mysqldatabasepath=C\:\\Program Files\\MySQL\\MySQL Server 5.6\\bin\\

#Zeitstempel des letztes Laufs (wird automatisch aktualisiert)
lastrun=2015-02-28 14\:30\:21

#EPG Begriff, mit dem festgestellt wird, dass das Programm eine Serie und kein Film ist. (Das deutsche EPG beginnt mit "Folge:", wenn man Clickfinder verwendet)
epgdescriptionseriesindicator=Folge\:

#EPG Beschreibungs Suchbegriff, um Serien- und Episoden-Nummern aus dem Beschreibungs-Text zu extrahieren, wenn im Offline-Modus gearbeitet wird, oder die Serie auf thetvdb.com nicht gefunden wurde. 
epgdescriptionpattern=Dies ist die \\d{1,}\\. Episode der \\d{1,}\\. Staffel.*

#MediaPortal Datenbank-Nutzer
mediaportaldbuser=root

#Top Level Qualifier deines Landes (Um den Spiegel-Server von thetvdb.com auszuwählen)
tld=de

#Sprache deines EPG
language=de

# Mediaportal Datenbank-Passwort
mediaportaldbpassword=

# Backup-Pfad (Vor jedem Lauf wird eine Sicherung angelegt)
backuppath=bak/

# Datenbank-Hostname
mediaportaldbhost=localhost

# Name der Datenbank
mediaportaldbname=mptvdb

# Thetvdb api proxy url
thetvdb.proxy=http://science-site.de:9000/thetvdb/

# Offline mode
offline=true

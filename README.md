# SnapSort

A litte hacky tool to rename camera files by date to automate picture sorting.

Date sources can be EXIF, file format, or file system date if needed.

## Usage Example
If `/path/to/source/` contains
```
├─ 20160530_210359.JPEG
├─ 2024-05-30 21-03-59.jpg
├─ 2024-05-30 21.03.59.jpg
├─ 20240530_210359.jpg
├─ 20240530_210412.mp4
└─ 4e69kpxgr88pm9pkjbr1b76far.jpg
```
(consider `4e69kpxgr88pm9pkjbr1b76far.jpg` has an exif date of `2016-09-19 21.11.12`)

And you run
```bash
java -jar snapsort-1.0.0-SNAPSHOT-runner.jar \
--input-folders /path/to/source/ \
--output-folder /path/to/move/result/ \
--write
```

Those files will be renamed and moved to the following data structure under `/path/to/move/result/`:
```
├─ 2016
│  ├─ 20160530_
│  │  └─ 2016-05-30 21.03.59.jpg
│  └─ 20160919_
│      └─ 2016-09-19 21.11.12.jpg
└─ 2024
   └─ 20240530_
      ├─ 2024-05-30 21.03.59.jpg
      ├─ 2024-05-30 21.04.00.jpg
      ├─ 2024-05-30 21.04.01.jpg
      └─ 2024-05-30 21.04.12.mp4
```
Notice that the date conflict between `2024-05-30 21.03.59.jpg` and `20240530_210359.jpg` has been resolved by advancing the time by 1 second for one of the two files.

Files are shifted like this when there is a conflict until there is no more conflict.

## More details
The code recursively lists multimedia files (mkv, jpeg, jpg, avi, mp4, mov, png, webp, gif) from the input folder.

For each file, it tries to determine the date the picture or video was taken. Date sources are:
- **File name:** Various formats are supported
- **EXIF data**
- **Filesystem modified date:** If `--read-filesystem-date-modified` is provided (this is a bit dangerous because it will rename even invalid files)


It is possible to restrict the list of files that get included with the parameters `--date-min` and `--date-max` (format is ISO, `yyyy-MM-dd`).

When there are several valid date sources for one file and they differ, the dates that are further away from the average date are eliminated until there is only one remaining.

The code then deduplicates the files by shifting the dates of the duplicated files by 1 second in the future until there is no more conflict.

When everything is OK and if the `--write` parameter is provided, the files are moved to the folder specified by the parameter `--output-folder` with this structure:
```
/yyyy/yyyyMMdd_/yyyy-MM-dd HH.mm.ss.extension
```

The extension is lowercase, and `jpeg` is renamed to `jpg`.

## Command line options
| Option                                  | Description                                                                        |
|-----------------------------------------|------------------------------------------------------------------------------------|
| `--input-folders=<inputFolders>`        | Mandatory, Input folders list, separated by commas                                 |
| `--output-folder=<outputFolder>`        | Mandatory, Output folder                                                           |
| `--date-max=<dateMax>`                  | Optional, Max date of range of acceptable dates                                    |
| `--date-min=<dateMin>`                  | Optional, Min date of range of acceptable dates                                    |
| `--write`                               | Optional, If no given, will do a dry run, no files will be written                 |
| `--read-filesystem-date-modified`       | Optional, Allow renaming according to filesystem dates modified (as a last resort) |
| `-V`, `--version`                       | Print version information and exit                                                 |

## Building
This applications is built with [Quarkus](https://quarkus.io/).
You need:
- Java 21
- Maven > 3.9.5

To build the jar:
```bash
mvn clean install
```
Will produce `target/snapsort-1.0.0-SNAPSHOT-runner.jar`

To build the native image:
```bash
mvn clean install -Pnative
```
Will produce binary `target/snapsort-1.0.0-SNAPSHOT-runner`

/*
 * Copyright (c) 2018 CPPAlien
 *
 * Licensed under the GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://github.com/CPPAlien/FileTransfer/blob/master/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.pengtao.filetransfer.util;

/**
 * @author CPPAlien
 */
public class FileType {
    public static final String FileTypes[][] = {
            // File Image
            {".png", ".jpg", ".jpeg", ".gif", ".bmp"},
            // File Audio
            {".mp3", ".wav", ".ogg", "midi"},
            // File Video
            {".mp4", ".rmvb", ".avi", ".flv", ".3gp"},
            // File Web Text
            {".jsp", ".html", ".htm", ".js", ".php"},
            // File Text
            {".txt", ".c", ".cpp", ".xml", ".py", ".json", ".log"},
            // File Excel
            {".xls", ".xlsx"},
            // File Word
            {".doc", ".docx"},
            // File PPT
            {".ppt", ".pptx"},
            // File PDF
            {".pdf"},
            // File Package
            {".jar", ".zip", ".rar", ".gz"},
            // APK
            {".apk"}};
    public static final int TYPE_IMAGE = 100;
    public static final int TYPE_AUDIO = 200;
    public static final int TYPE_VIDEO = 300;
    public static final int TYPE_WEB = 400;
    public static final int TYPE_TEXT = 500;
    public static final int TYPE_EXCEL = 600;
    public static final int TYPE_WORD = 700;
    public static final int TYPE_PPT = 800;
    public static final int TYPE_PDF = 900;
    public static final int TYPE_PACKAGE = 1000;
    public static final int TYPE_APK = 1100;
    public static final int TYPE_UNKNOWN = -1;
    public static final int TypeStart[] = {TYPE_IMAGE, TYPE_AUDIO, TYPE_VIDEO,
            TYPE_WEB, TYPE_TEXT, TYPE_EXCEL, TYPE_WORD, TYPE_PPT, TYPE_PDF, TYPE_PACKAGE, TYPE_APK};
}

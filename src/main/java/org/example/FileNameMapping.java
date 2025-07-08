package org.example;

public class FileNameMapping {
    public static String fileCSVToExcel(String csvFileName) {
        switch (csvFileName) {
            case "Testcase_Account.csv" -> {
                return "Testcase_Account";
            }
            case "Testcase_AR-WC.csv" -> {
                return "Testcase_AR-WC";
            }
            case "Testcase_DetailVOD.csv" -> {
                return "Testcase_DetailVOD";
            }
            case "Testcase_DangXem.csv" -> {
                return "Testcase_DangXem";
            }
            case "Testcase_Home.csv" -> {
                return "Testcase_Home";
            }
            case "Testcase_Login.csv" -> {
                return "Testcase_Login";
            }
            case "Testcase_Multiprofile.csv" -> {
                return "Testcase_Multiprofile";
            }
            case "Testcase_Player.csv" -> {
                return "Testcase_Player";
            }
            case "Testcase_Playlist.csv" -> {
                return "Testcase_Playlist";
            }
            case "Testcase_Preview5p.csv" -> {
                return "Testcase_Preview5p";
            }
            case "Testcase_Search.csv" -> {
                return "Testcase_Search";
            }
            case "Testcase_SuKien.csv" -> {
                return "Testcase_SuKien";
            }
            case "Testcase_TheLoai.csv" -> {
                return "Testcase_TheLoai";
            }
            case "Testcase_TheThao.csv" -> {
                return "Testcase_TheThao";
            }
            case "Testcase_Timeshift-Catchup.csv" -> {
                return "Testcase_Timeshift-Catchup";
            }
            case "Testcase_Timeshift-Seek.csv" -> {
                return "Testcase_Timeshift-Seek";
            }
            case "Testcase_TruyenHinh.csv" -> {
                return "Testcase_TruyenHinh";
            }
            case "Testcase_TheoDoi.csv" -> {
                return "Testcase_TheoDoi";
            }
            case "Testcase_Setting.csv" -> {
                return "Testcase_Setting";
            }
            default -> throw new IllegalArgumentException("Invalid file name");
        }
    }
}

public class byteStuff {
   public static void main(String[] args) {

      int indexOfStartOfFilePath = args[0].indexOf("/");
      if (indexOfStartOfFilePath < 0) {
         System.out.println("Invalid input");
         return;
      }

      String filePath = args[0].substring(indexOfStartOfFilePath);
      String serverAndPort = args[0].substring(0, indexOfStartOfFilePath);

      System.out.println("serverAndPort: " + serverAndPort);
      System.out.println("serverAndPort length: " + serverAndPort.length());
      System.out.println("filePath: " + filePath);

      String[] serverAndPortSplit = serverAndPort.split(":");
      String server = serverAndPortSplit.length != 1 ? serverAndPortSplit[0] : "localhost";
      String port = serverAndPortSplit.length > 1 ? serverAndPortSplit[1].split("/")[0] : "69";

      int fileNameStartIndex = filePath.lastIndexOf("/") + 1;
      String dir = fileNameStartIndex > 1 ? filePath.substring(1, fileNameStartIndex) : "";
      String filename = fileNameStartIndex > 0 ? filePath.substring(fileNameStartIndex) : "";

      System.out.println("Sever: " + server);
      System.out.println("Port: " + port);
      System.out.println("Directory: " + dir);
      System.out.println("Filename: " + filename);

   }
}

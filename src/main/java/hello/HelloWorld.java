package hello;

import org.apache.spark.sql.*;
import static org.apache.spark.sql.functions.col;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class HelloWorld {

	public static class Cust implements Serializable {
		private int id;
		private String name;
		private double sales;
		private double discount;
		private String state;

		public Cust(int id, String name, double sales, double discount, String state) {
			this.id = id;
			this.name = name;
			this.sales = sales;
			this.discount = discount;
			this.state = state;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public double getSales() {
			return sales;
		}

		public void setSales(double sales) {
			this.sales = sales;
		}

		public double getDiscount() {
			return discount;
		}

		public void setDiscount(double discount) {
			this.discount = discount;
		}

		public String getState() {
			return state;
		}

		public void setState(String state) {
			this.state = state;
		}
	}

	public static class StateSales implements Serializable {
		private double sales;
		private String state;

		public StateSales(int id, String name, double sales, double discount, String state) {
			this.sales = sales;
			this.state = state;
		}

		public double getSales() {
			return sales;
		}

		public void setSales(double sales) {
			this.sales = sales;
		}

		public String getState() {
			return state;
		}

		public void setState(String state) {
			this.state = state;
		}
	}

    static String readFile(String path, Charset encoding)
    {
        try{
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, encoding).replace(System.getProperty("line.separator"),"");
        } catch(IOException e) {
            System.out.println("File Not Found: "+path);
        }
        return null;
    }

	public static void main(String[] args) throws Exception {
		System.out.println("Hello, World");
		System.out.println("System-Properties:");
		System.out.println("\t\\--propertyName1=" + System.getProperty("propertyName1"));
		System.out.println("\t\\--propertyName2=" + System.getProperty("propertyName2"));
//		final ImmutableCollection col = ImmutableList.of("hello","hello1"); // using guava lib

        String masterIP = readFile("/databricks/driver/driver_ip.txt", Charset.defaultCharset());

		SparkSession spark = SparkSession
				.builder()
				.appName("capsule-maven-plugin-demo")
                .master("spark://" + masterIP + ":7077")
                //.master("spark://127.0.0.1:7077")  //Connection refused: /127.0.0.1:7077
				.getOrCreate();

		Encoder<Cust> custEncoder = Encoders.bean(Cust.class);

		List<Cust> data = Arrays.asList(
				new Cust(1, "Widget Co", 120000.00, 0.00, "AZ"),
				new Cust(2, "Acme Widgets", 410500.00, 500.00, "CA"),
				new Cust(3, "Widgetry", 410500.00, 200.00, "CA"),
				new Cust(4, "Widgets R Us", 410500.00, 0.0, "CA"),
				new Cust(5, "Ye Olde Widgete", 500.00, 0.0, "MA")
		);

		Dataset<Cust> ds = spark.createDataset(data, custEncoder);

		System.out.println("*** here is the schema inferred from the Cust bean");
		ds.printSchema();

		System.out.println("*** here is the data");
		ds.repartition(5).show();

		Dataset<Row> smallerDF =
				ds.select("sales", "state").filter(col("state").equalTo("CA"));

		System.out.println("*** here is the dataframe schema");
		smallerDF.printSchema();

		System.out.println("*** here is the data");
		smallerDF.show();

		Encoder<StateSales> stateSalesEncoder = Encoders.bean(StateSales.class);

		Dataset<StateSales> stateSalesDS = smallerDF.as(stateSalesEncoder);

		System.out.println("*** here is the schema inferred from the StateSales bean");
		stateSalesDS.printSchema();

		System.out.println("*** here is the data");
		stateSalesDS.show();
	}


}

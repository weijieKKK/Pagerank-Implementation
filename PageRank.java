import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.*;

public class PageRank {

    class Edge {
        int dest;     // destination airport
        int weight;   // number of routes in this edge
    }

    class EdgeList {
        int weight;    // total number of edges = sum of second components of list
        ArrayList<Edge> list;
    }
    class Pair{
    	String first;
    	Double second;
    }

    static String airportCodes[];           // index to short code
    static String airportNames[];           // index to airport name
    static HashMap<String,Integer> airportIndices = new HashMap<String,Integer>();  // airport code to index
    static EdgeList[] G;             // G[i] is a list of pairs (j,k) meaning
                                     // "there are k routes from airport i to airport j"
    //....                             // other info??
    
    static HashMap<String,String> codeToName = new HashMap<String,String>();
    
    //dado un indice de un aeropuerto devuelve el peso de salida out(j)
    static HashMap<Integer,Integer> pesos_salida = new HashMap<Integer,Integer>(); 
    
    public static void readAirports() {
      try {
         String fileName = "./airports.txt";
         System.out.println("... opening file "+fileName);
         FileInputStream fstream = new FileInputStream(fileName);
         DataInputStream in = new DataInputStream(fstream);
         BufferedReader br = new BufferedReader(new InputStreamReader(in));
         
         String strLine;
         int index = 0;
         ArrayList<String> codeTemp = new ArrayList<String>();
         while ((strLine = br.readLine()) != null) { 
        	   //System.out.println(strLine);
               String[] aLine = strLine.split(",");
               String airportCode = aLine[4];
               //airportCode = airportCode.substring(1, 3);
               
               String airportName = aLine[1]+" ("+aLine[3]+")";
               if (airportCode.length() == 5) {
            	   //System.out.println(airportCode.substring(1, 4));
            	   
                   codeTemp.add(airportCode.substring(1, 4));
                  // System.out.println("codigo " + airportCode + " " +airportCode.length());
                   index++;
                   if(!codeToName.containsKey(airportCode.substring(1, 4))){
                	   codeToName.put(airportCode.substring(1, 4), airportName);
                   }
                  
                }
         }
         
         G = new EdgeList[index];
         PageRank p = new PageRank();
         for(int i = 0; i < codeTemp.size(); ++i){
        	 airportIndices.put(codeTemp.get(i), i);
        	 G[i] = p.new EdgeList();
        	 G[i].list = new ArrayList<Edge>();
        	 G[i].weight = 0;

         }

         // TO DO: DUMP STUFF TO airportCodes, airportNames, airportIndices
         
         System.out.println("... "+index+" airports read");

         in.close();
         
       } catch (Exception e){
		     //Catch exception if any
             System.err.println("Error: " + e.getMessage());
             e.printStackTrace();
             // return null;
       }
    
    }


   public static void readRoutes() {
	   try {
	         String fileName = "./routes.txt";
	         System.out.println("... opening file "+fileName);
	         FileInputStream fstream = new FileInputStream(fileName);
	         DataInputStream in = new DataInputStream(fstream);
	         BufferedReader br = new BufferedReader(new InputStreamReader(in));
	         PageRank p = new PageRank();
	         String strLine;
	         int index = 0;
	         while ((strLine = br.readLine()) != null) {           
	               String[] aLine = strLine.split(",");
	               String airportOrigen = aLine[2];
	               String airportDestino = aLine[4];
	               if (airportIndices.containsKey(airportDestino) && airportIndices.containsKey(airportOrigen)) {
	            	   Integer origen_code = airportIndices.get(airportOrigen);
	            	   Integer destino_code = airportIndices.get(airportDestino);
	            	  // System.out.println("Entro");
	            	   Edge e = p.new Edge();
	            	   e.weight = 1;
	            	   e.dest = origen_code;
	            	   ++index;
	            	   G[destino_code].list.add(e);
	            	   
	            	   if(pesos_salida.containsKey(origen_code)){
	            		   Integer aux = pesos_salida.get(origen_code);
	            		   pesos_salida.put(origen_code,aux+1 ); 
	            	   }
	            	   else pesos_salida.put(origen_code,1 );

	               }
	         }
	         
	         
	         System.out.println("... "+index+" routes read");

	         in.close();
	         
	       } catch (Exception e){
			     //Catch exception if any
	             System.err.println("Error: " + e.getMessage());
	             e.printStackTrace();
	             // return null;
	       }
	    
   }

   public static Double[] computePageRanks() {
      
	   int n = G.length;
	   double L = 0.85;
	   Double[] P = new Double[n];
	   Arrays.fill(P, 1.0/n);
	   Double [] Q = new Double[n];
	   Arrays.fill(Q,0.0);
	   int j = 0;
	   boolean continua = true;
	   while(continua){
		   ++j;
		   Arrays.fill(Q,0.0);

		   Double pagerank_acumulado_nodos_sin_aristas_salida = 0.0;
		   Double sum = 0.0;
		   
		   for(int i = 0; i < n; ++i){
			   if(!pesos_salida.containsKey(i)){
				   pagerank_acumulado_nodos_sin_aristas_salida += P[i] / n;
			   }
		   }
		   for(int i = 0; i < n; ++i){
			   Q[i] += pagerank_acumulado_nodos_sin_aristas_salida;  
		   }
		   
		   //System.out.println("pr " + pagerank_acumulado_nodos_sin_aristas_salida);
		   for(int i = 0; i < n; ++i){ //for each node in G
			   sum = 0.0;
			   if(pesos_salida.containsKey(i)){ //calculem pagerank normalment si te alguna aresta de sortida
				   for(int k = 0; k < G[i].list.size(); ++k){
					   sum += P[G[i].list.get(k).dest] / pesos_salida.get(G[i].list.get(k).dest);
				   }
				   Q[i] += sum;
				   
			   }
			   Q[i] = L * Q[i] + (1-L)/n;
			  
		   }
		   continua = resta(P,Q,0.00000000000001);
		   System.arraycopy(Q, 0, P, 0, n);
		   
		   
		   double aux = 0.0;
		   for(int y = 0; y < P.length; ++y){
			   aux += P[y];
		   }
		   //System.out.println("suma " + aux);
		   //System.out.println("iters" + j);
		   
	   }
	   
	   
	   return P;
	   
   }
   
   private static boolean resta(Double[] a, Double[] b, double factor){
	   double maxdif = b[0]-a[0];
	   for(int k = 0; k < a.length; ++k){
		   if (b[k] > a[k]){
			   if(b[k] - a[k] > maxdif){
				   maxdif = b[k] - a[k];
			   }
				   
		   }
		   else{
			   if(a[k] - b[k] > maxdif){
				   maxdif = a[k] - b[k];
			   }
				   
		   }
			 
	   }
	   if(maxdif > factor) return true;
	   //System.out.println("maxdif " + maxdif + " factor " + factor);
	   return false;
   }
   

   public static void outputPageRanks(Double[] p) throws FileNotFoundException, UnsupportedEncodingException {
	   PrintWriter writer = new PrintWriter("./salida.txt", "UTF-8");
	   
	   PageRank p1 = new PageRank();
	  
	   Pair[] arraySolucion = new Pair[airportIndices.keySet().size()];
	   int it = 0;
	   for(String a : airportIndices.keySet()){
		   Pair aux = p1.new Pair();
		   aux.first = codeToName.get(a);
		   aux.second = p[airportIndices.get(a)];
		   arraySolucion[it] = aux;
		   ++it;
	   }
	
	   Arrays.sort(arraySolucion, p1.new CustomComparator());
	   
	   for(int i = 0; i < it; ++i){
		   
		   writer.println("Airport: "+ arraySolucion[i].first + " PR: " + arraySolucion[i].second);
	   }
	   writer.close();
   }
   
   public class CustomComparator implements Comparator<Pair> {
	    public int compare(Pair p1, Pair p2) {
	    	//System.out.println("Pair 1 first: " + p1.first + " second: "+p1.second);
	    	//System.out.println("Pair 2 first: " + p2.first + " second: "+p2.second);
	        return p1.second < p2.second ? 1 : -1;
	    }
	}
   
   

   public static void main(String args[]) throws FileNotFoundException, UnsupportedEncodingException  {

       readAirports();   // get airport names, codes, and assign indices
       readRoutes();     // read tuples and build graph
       Double[] PRSolution = computePageRanks();
       outputPageRanks(PRSolution); 

    }
    
}

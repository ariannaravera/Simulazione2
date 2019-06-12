package it.polito.tdp.food.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.food.db.Condiment;
import it.polito.tdp.food.db.Food;
import it.polito.tdp.food.db.FoodDao;

public class Model {
	
	private FoodDao dao;
	private Graph<Condiment, DefaultWeightedEdge> grafo;
	private Map<Integer,Condiment> condimenti;
	private Map<Integer,Food> cibi;
	private List<Condiment> parziale;
	private List<Condiment> best;
	private List<Condiment> collegati;
	private List<Condiment> nonCollegati;
	
	public Model(){
		dao=new FoodDao();
		condimenti=new HashMap<>();
		for(Condiment c:dao.listAllCondiment()) {
			condimenti.put(c.getCondiment_id(), c);
		}
		cibi=new HashMap<>();
		for(Food f:dao.listAllFood()) {
			cibi.put(f.getFood_id(), f);
		}
	}

	public String grafo(double calorie) {
		grafo=new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		for(Condiment c:condimenti.values()) {
			if(c.getCondiment_calories()<calorie) 
				grafo.addVertex(c);
		}
		for(Condiment c1:grafo.vertexSet()) {
			for(Condiment c2:grafo.vertexSet()) {
				if(!c1.equals(c2)) {
					int n=dao.getPeso(c1.getFood_code(),c2.getFood_code());
					if(n!=0) {
						Graphs.addEdgeWithVertices(grafo, c1, c2, n);
					}
				}
			}
		}
		System.out.println(grafo.vertexSet().size()+" "+grafo.edgeSet().size());
		/*
		 * Si visualizzi, per ciascuno degli ingredienti (ordinati per calorie decrescenti), 
		 * il numero di calorie corrispondente ed il numero totale di cibi che lo contengono 
		 * (tenendo conto del peso di tutti gli archi incidenti). 
		 */
		String s="";
		List<Condiment> listac=new ArrayList<>(grafo.vertexSet());
		Collections.sort(listac);
		for(Condiment c:listac) {
			int somma=0;
			for(DefaultWeightedEdge e:grafo.edgesOf(c))
				somma+=grafo.getEdgeWeight(e);
			s+=c.getCondiment_id()+" - "+c.getDisplay_name()+", calorie: "+c.getCondiment_calories()+", contenuto in "+somma+" cibi.\n";
		}
		return s;
	}
	
	public List<Condiment> condimenti(){
		List<Condiment> cond=new ArrayList<>(grafo.vertexSet());
		return cond;
	}

	public String getCammino(Condiment c) {
		

		collegati=new ArrayList<>(Graphs.neighborListOf(grafo, c));
		nonCollegati=new ArrayList<>(grafo.vertexSet());
		nonCollegati.removeAll(collegati);
		
		/*
    	 *  si calcoli un insieme di ingredienti indipendenti tra loro che apportino il massimo numero di calorie
    	 *  CAMMINO MASSIMO 
    	 *  e che contengano il vertice selezionato. Gli ingredienti dell’insieme devono essere a due a due indipendenti, 
    	 *  cioè non devono mai comparire nello stesso cibo (cioè non devono essere adiacenti). Nota: nel calcolo 
    	 *  dell’insieme di vertici indipendenti, si lavori partendo da grafi di piccole dimensioni. 
    	 */
		
		
		best=new ArrayList<>();
		
		for(Condiment c2:nonCollegati) {
			parziale=new ArrayList<>();
			parziale.add(c2);
			ricorsione(c2,parziale,c);
		}
		
		String s="";
		for(Condiment c1:best) {
			s+=c1.getCondiment_id()+" - "+c1.getDisplay_name()+"\n";
		}
		return s;
	}

	private void ricorsione(Condiment in, List<Condiment> parziale, Condiment cond) {
		if(parziale.contains(cond)) {
			if(parziale.size()>best.size()) {
				best=new ArrayList<>(parziale);
			}
		}
		for(Condiment c:nonCollegati) {
			if(!parziale.contains(c)) {
				parziale.add(c);
				ricorsione(c, parziale, cond);
				parziale.remove(parziale.size()-1);
			}
		}
		
		
	}

}

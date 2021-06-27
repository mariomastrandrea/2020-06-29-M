package it.polito.tdp.imdb;

import java.net.URL;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import it.polito.tdp.imdb.model.Director;
import it.polito.tdp.imdb.model.Model;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class FXMLController 
{
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnCreaGrafo;

    @FXML
    private Button btnAdiacenti;

    @FXML
    private Button btnCercaAffini;

    @FXML
    private ComboBox<Year> boxAnno;

    @FXML
    private ComboBox<Director> boxRegista;

    @FXML
    private TextField txtAttoriCondivisi;

    @FXML
    private TextArea txtResult;
    
    private Model model;
    

    @FXML
    void doCreaGrafo(ActionEvent event) 
    {
    	Year selectedYear = this.boxAnno.getValue();
    	
    	if(selectedYear == null)
    	{
    		this.txtResult.setText("Errore: selezionare un anno dal menù a tendina");
    		return;
    	}
    	
    	this.model.createGraph(selectedYear);
    	
    	//print
    	int numVertices = this.model.getNumVertices();
    	int numEdges = this.model.getNumEdges();
    	
    	String output = this.printGraphInfo(numVertices, numEdges);
    	this.txtResult.setText(output);
    	
    	//update UI
    	List<Director> directors = this.model.getOrderedDirectors();
    	this.boxRegista.getItems().clear();
    	this.boxRegista.getItems().addAll(directors);
    }

    private String printGraphInfo(int numVertices, int numEdges)
	{
		return String.format("Grafo creato\n#Vertici: %d\n#Archi: %d", numVertices, numEdges);
	}

	@FXML
    void doRegistiAdiacenti(ActionEvent event) 
    {
		if(!this.model.isGraphCreated())
		{
			this.txtResult.setText("Errore: creare prima il grafo");
			return;
		}
		
		Director selectedDirector = this.boxRegista.getValue();
		
		if(selectedDirector == null)
		{
			this.txtResult.setText("Errore: selezionare un regista dal menù a tendina");
			return;
		}
		
		Map<Director, Integer> adjacentDirectors = 
				this.model.getAdjacentDirectorsTo(selectedDirector);
		
		List<Director> orderedDirectors = new ArrayList<>(adjacentDirectors.keySet());
		orderedDirectors.sort((d1, d2) -> 
		{
			int commonActors1 = adjacentDirectors.get(d1);
			int commonActors2 = adjacentDirectors.get(d2);
			
			return Integer.compare(commonActors2, commonActors1);
		});
		
		String output = this.printDirectorsActors(selectedDirector, orderedDirectors, adjacentDirectors, 5);
		this.txtResult.setText(output);
    }

    private String printDirectorsActors(Director selectedDirector, List<Director> orderedDirectors, 
    		Map<Director, Integer> adjacentDirectors, int max)
	{
    	StringBuilder sb = new StringBuilder();
    	sb.append("Registi adiacenti a ").append(selectedDirector).append(":");
    	int count = 0;
    	
		for(Director director : orderedDirectors)
		{
			count++;
			int numActors = adjacentDirectors.get(director);
			
			sb.append("\n • ").append(director.toString())
				.append("  -  # attori condivisi: ").append(numActors);
			
			if(count >= max) break;
		}
		
		return sb.toString();
	}

	@FXML
    void doRicorsione(ActionEvent event) 
    {

    }

    @FXML
    void initialize() 
    {
        assert btnCreaGrafo != null : "fx:id=\"btnCreaGrafo\" was not injected: check your FXML file 'Scene.fxml'.";
        assert btnAdiacenti != null : "fx:id=\"btnAdiacenti\" was not injected: check your FXML file 'Scene.fxml'.";
        assert btnCercaAffini != null : "fx:id=\"btnCercaAffini\" was not injected: check your FXML file 'Scene.fxml'.";
        assert boxAnno != null : "fx:id=\"boxAnno\" was not injected: check your FXML file 'Scene.fxml'.";
        assert boxRegista != null : "fx:id=\"boxRegista\" was not injected: check your FXML file 'Scene.fxml'.";
        assert txtAttoriCondivisi != null : "fx:id=\"txtAttoriCondivisi\" was not injected: check your FXML file 'Scene.fxml'.";
        assert txtResult != null : "fx:id=\"txtResult\" was not injected: check your FXML file 'Scene.fxml'.";
    }
    
    public void setModel(Model model)
    {
    	this.model = model;
    	this.boxAnno.getItems().addAll(Year.of(2004), Year.of(2005), Year.of(2006));
    }
}


package template;

import java.util.List;
import java.util.Random;

import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class ReactiveTemplate implements ReactiveBehavior {

	private Random random;
	private double pPickup;
	private int numActions;
	private Agent myAgent;
	private int number_of_cities;
	private double [][] probabiltyTaskFromTo;
	private double [][] rewardTaskFromTo;
	//state
	//private City [][] state;
	private List<City> cities;  

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);

		this.random = new Random();
		this.pPickup = discount;
		this.numActions = 0;
		this.myAgent = agent;
		this.number_of_cities = topology.size();
		this.cities =  topology.cities();
		
		//set the probability tab of a task from city i to go to city j and
		//set the rewards tab of a task from a city to an other
		
		//this.probabiltyTaskFromTo[number_of_cities][number_of_cities]= (Double) null;
		for(int i=0; i<number_of_cities; i++) {
			for(int j=0; j<number_of_cities; j++) {
				City a = topology.cities().get(i); //use cities.get
				City b= topology.cities().get(j);
				
				this.probabiltyTaskFromTo[i][j]= td.probability(a, b);
				
				this.rewardTaskFromTo[i][j]= td.reward(a, b);
			}
		}
		
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		//Action action = acceptOrRefuse(vehicle, availableTask);
		
		Action action;
		/*if (availableTask == null || random.nextDouble() > pPickup) {
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));}
		else {
			 action=  acceptOrRefuse(availableTask.pickupCity.id, availableTask.deliveryCity.id);
		}*/
		// null task. move to neighbor
		
		if (availableTask == null || random.nextDouble() > pPickup) {
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));
			System.out.println(action.toString());
		} else {
			
			// found task? accept or refuse?
			//our function to decide if it takes a task or not 
			//if(acceptOrRefuse(availableTask)){
				action = new Pickup(availableTask);
			}
			else
			System.out.println(action.toLongString());

		}
		
		if (numActions >= 1) {
			System.out.println("The total profit after "+numActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)numActions)+")");
		}
		numActions++;
		
		return action;
	}
	
	private boolean acceptOrRefuse(int pickup, int delivery) {
		boolean accept = false;
		
		Action action;
		//accept the task
		double q1 = vIteration(pickup,delivery, true);
		//refuse task 
		double q2 = 0;
		double q2_comp=0;
		for(int i=0;i<number_of_cities; i++) {
			q2_comp = vIteration(pickup,i,false);
			if(q2_comp>q2)
				q2=q2_comp;
		}
		
		if(q1>q2)
			accept= true;
		
		/*V1 = availableTask.reward;
		for(int i=0;i<number_of_cities; i++) {
		V1 += probabiltyTaskFromTo[availableTask.deliveryCity.id][i]
				*vIteration(availableTask.pickupCity.id,availableTask.deliveryCity.id);
		}*/
		return accept;
	}
	private double vIteration(int pickup, int delivery) {
		double V1=0;
		double V1_next=0;
		double V2=0;
		double V2_next=0;
		//accept ask
		V1 = rewardTaskFromTo[pickup][delivery];
		for(int i=0;i<number_of_cities; i++) {
			V1 += probabiltyTaskFromTo[delivery][i]
				*vIteration(delivery,i);
		}
		//refuse task 
		while(true) {
			for(int i=0;i<number_of_cities; i++) {
				V2_next = V2+ probabiltyTaskFromTo[pickup][i]
					*vIteration(pickup,i);
				if((V2_next-V2)<4)
					break;
				else V2 = V2_next;
			}
		}
		if(V1>V2)
			return V1;
		else 
			return V2;
	}
		
	/*private double rewards_from_state(Vehicle vehicle) { 
		double rewards= 0;
		///V in the lecture 
		City currentCity = vehicle.getCurrentCity();
		for(int i=0; i<number_of_cities; i++ ) {
			int distance = 
		}
		
		return rewards;
	}*/
	
}

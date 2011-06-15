import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;

class Util {
	public static int CompareInt(int l, int r) {
		if (l < r)
			return -1;
		if (l == r)
			return 0;
		return 1;
	}
}
class Machine implements Comparable<Machine>
{
	public Machine(String name, Compound input, Compound output, int price) {
		m_name = name;
		m_input = input;
		m_output = output;
		m_price = price;
	
		String resultAsStr = m_name.substring(1);
		m_number = Integer.parseInt(resultAsStr);
	}
	
	public String toString() {
		return String.format("%s - %s : %,d", m_input.Name(), m_output.Name(), m_price);
	}
	
	public Compound Output() { return m_output; }

	public Compound Input() { return m_input; }
	
	public int Price() { return m_price; }
	
	public int Number() {
		return m_number;
	}
	
	private String m_name;
	private Compound m_input;
	private Compound m_output;
	private int m_price;
	private int m_number;

	public int compareTo(Machine o) {
		return Util.CompareInt(m_number, o.m_number);
	}
}


class Compound {
	public Compound(String name, int index) {
		m_name = name;
		m_index = index;
		m_consumers = new Machine[0];
		m_producers = new Machine[0];
		
		m_minProducerCost = Integer.MAX_VALUE;
		m_minConsumerCost = Integer.MAX_VALUE;
	}
	
	public String Name() { return m_name; }

	public void AddConsumer(Machine m) {
		assert m.Input() == this;
		m_consumers = Append(m_consumers, m);
		
		m_minConsumerCost = Math.min(m_minConsumerCost, m.Price());
	}

	public void AddProducer(Machine m) {
		assert m.Output() == this;
		m_producers = Append(m_producers, m);
		
		m_minProducerCost = Math.min(m_minProducerCost, m.Price());
	}
	
	public int Index() { return m_index; }
	
	public Machine[] Consumers() { return m_consumers; }
	
	public Machine[] Producers() { return m_producers; }
	
	private Machine[] Append(Machine[] src, Machine newEntry) {
		Machine[] result = Arrays.copyOf(src, src.length + 1);
		result[src.length] = newEntry;
		return result;
	}
	
	private String m_name;
	private int m_index;
	
	private Machine[] m_consumers;
	private Machine[] m_producers;
	
	private int m_minProducerCost;
	private int m_minConsumerCost;
	
	public long Mask() {
		return 1L << m_index;
	}

	public int MinProducerCost() {
		return m_minProducerCost;
	}

	public long ConsumersMask() {
		long result = 0;
		for (Machine m : m_producers)
			result |= m.Input().Mask();
		
		return result;
	}

	public int MinConsumerCost() { return m_minConsumerCost; }
}

class Result {
	public Result(Machine[] machines) {
		m_machines = machines;
		Arrays.sort(m_machines);
		
		for (Machine m : machines) 
			m_totalPrice += m.Price();
	}
	
	public int TotalPrice() { return m_totalPrice; }
	
	public String toString() {
		String result = "";
		for (Machine m : m_machines) { 
			result += m.Number();
			result += " ";
		}
		return result;
	}
	
	private int m_totalPrice;
	private Machine[] m_machines;
	public Machine[] RequiredMachines() { return m_machines; }
}


public class FaceBullMain {

	public FaceBullMain(String inputFile) throws IOException {
		Scanner rd = new Scanner(new FileInputStream(inputFile));

		HashMap<String, Compound> nameToCompound = new HashMap<String, Compound>();
		m_machines = new HashMap<String, Machine>(); 

		for (;;)
		{
			try
			{
				String machineId = rd.next();
				String fromName = rd.next();
				String toName = rd.next();
				int price = rd.nextInt();
				
				Compound from = FindCompound(fromName, nameToCompound);
				Compound to = FindCompound(toName, nameToCompound);
				
				Machine m = new Machine(machineId, from, to, price);
				from.AddConsumer(m);
				to.AddProducer(m);
				
				m_machines.put(machineId, m);				
			}
			catch (Exception e) 
			{
				break;
			}
		}
		
		m_compounds = new Compound[nameToCompound.size()];
		for (Compound c : nameToCompound.values())
			m_compounds[c.Index()] = c;
		
		m_minProducerCost = new int[m_compounds.length + 1];
		m_minProducerCost[m_compounds.length] = 0;
		for (int i = m_compounds.length-1; i >= 0; i--)
			m_minProducerCost[i] = m_minProducerCost[i + 1] + m_compounds[i].MinProducerCost();
		
		m_remainingConsumers = new long[m_compounds.length+1];
		m_remainingConsumers[m_compounds.length] = 0;
		for (int i = m_compounds.length-1; i >= 0; i--)
			m_remainingConsumers[i] = m_remainingConsumers[i + 1] | m_compounds[i].ConsumersMask();
		
	}
	
	private static boolean IsConnected(int[][] minCostMatrix) {
		for (int[] row : minCostMatrix)
			for (int cell : row)
				if (cell == Integer.MAX_VALUE)
					return false;
		
		return true;
	}
	
	class State implements Comparable<State> {
		public State() {
			m_machines = new ArrayList<Machine>();
			m_connectivity = new long[m_compounds.length];
			// Even with no machines we can get from each compound to itself
			for (Compound c : m_compounds)
				m_connectivity[c.Index()] = c.Mask();
			
			m_cost = 0;
			m_penalty = 0;
			m_iNextCompound = 0;	
		}

		public State(ArrayList<Machine> machines, long[] connectivity, int cost, int iNextCompound) {
			m_machines = new ArrayList<Machine>(machines);
			m_connectivity = connectivity;
			m_cost = cost;
			m_iNextCompound = iNextCompound;
			
			// Penalty based just on producers we need yet
			m_penalty = m_minProducerCost[m_iNextCompound];

			long consumed = 0;
			for (Machine m : machines)
				consumed |= m.Input().Mask();
			
			long availableConsumers = consumed | m_remainingConsumers[m_iNextCompound];

			int consumerPenalty = 0;
			if ((availableConsumers) != m_fullMask) {
				consumerPenalty = Integer.MAX_VALUE;
			}
			else {
				for (Compound c : m_compounds)
					if ((availableConsumers & c.Mask()) == 0)
						consumerPenalty += c.MinConsumerCost();
			}
			
			m_penalty = Math.max(m_penalty, consumerPenalty);
		}
		
		private ArrayList<Machine> m_machines;
		private long[] m_connectivity;
		private int m_cost;			// Add penalty for min cost of all further machines
		private int m_iNextCompound;
		private int m_penalty;

		public int compareTo(State o) {
			return Util.CompareInt(m_cost + m_penalty, o.m_cost + o.m_penalty);
		}
		
		public String toString() {
			return String.format("%s; %,d; %s", m_machines.toString(), m_cost, Arrays.toString(m_connectivity));
		}
		
		public void AddChildren(PriorityQueue<State> queue) {
			if (m_iNextCompound >= m_compounds.length)
				return;
			
			Compound c = m_compounds[m_iNextCompound];

			Machine[] producers = c.Producers();
			ArrayList<Machine> childUsedMachines = new ArrayList<Machine>(m_machines);
			for (int i = 0; i < producers.length; i++) {
				Machine p = producers[i];
				
				assert IsNewConnection(m_connectivity, p); 

				childUsedMachines.add(p);
				long[] childCon = UpdateConnectivity(m_connectivity, p);
				
				EnumCombos(producers, childCon, i+1, childUsedMachines, m_cost + p.Price(), queue);

				childUsedMachines.remove(childUsedMachines.size() - 1);
			}
		}

		private void EnumCombos(Machine[] producers, long[] connectivity, int iFirst, ArrayList<Machine> usedMachines, int totalCost,
				PriorityQueue<State> q) 
		{
			if (iFirst == producers.length) {
				State child = new State(usedMachines, connectivity, totalCost, m_iNextCompound + 1);
				if (child.Penalty() == Integer.MAX_VALUE)
					m_countInvalidStates++;
				else
					q.add(child);
				
				return;
			}
			
			// Combinations without iFirst
			EnumCombos(producers, connectivity, iFirst + 1, usedMachines, totalCost, q);

			// Combinations with iFirst
			Machine p = producers[iFirst];
			if (IsNewConnection(connectivity, p)) {
				
				long[] newCon = UpdateConnectivity(connectivity, p);
				
				usedMachines.add(p);
				EnumCombos(producers, newCon, iFirst + 1, usedMachines, totalCost + p.Price(), q);
				usedMachines.remove(usedMachines.size() - 1);
			}
		}

		private boolean IsNewConnection(long[] connectivity, Machine p) {
			long existingOutputsMask = connectivity[p.Input().Index()];
			long thisOutputMask = existingOutputsMask & p.Output().Mask(); 
			return thisOutputMask == 0;
		}

		private long[] UpdateConnectivity(long[] connectivity, Machine m) {
			long[] result = connectivity.clone();
			
			long possibleOutputs = connectivity[m.Output().Index()];
			
			for (int iCompound = 0; iCompound < m_compounds.length; iCompound++) {
				if ((connectivity[iCompound] & m.Input().Mask()) != 0) {
					// Can go from c to m.Input
					// Thus, with m can go from c to anywhere m.Output can go
					result[iCompound] = result[iCompound] | possibleOutputs;
				}
			}
			
			return result;
		}

		public boolean IsFullyConnected() {
			for (long l : m_connectivity)
				if (l != m_fullMask)
					return false;
			return true;
		}

		public Machine[] MachinesUsed() {
			return m_machines.toArray(new Machine[m_machines.size()]);
		}

		public int  Cost() { return m_cost; }

		public int Penalty() { return m_penalty; }
	}
	
	private Result BreathFirst() {
		PriorityQueue<State> q = new PriorityQueue<State>();
		q.add(new State());
		Runtime runtime = Runtime.getRuntime();
		
		s_log.printf("Mem memory = %,d [MB]\n", runtime.maxMemory() / 1024 / 1024);
		
		int popCount = 0;
		int shortcutCount = 0;
		
		for (;;) {
			State top = q.poll();
			if (top == null)
				break;
			
			popCount++;
			
			if ((popCount % 100) == 0) {
				long usedMem = runtime.totalMemory() - runtime.freeMemory();
				usedMem /= 1024 * 1024;
				s_log.printf("PopCount %,d : Top Cost = %,d; Penalty = %,d; \nQueue Size = %,d; #Invalid = %,d; Mem %,d[MB]\n", popCount, 
						top.Cost() + top.Penalty(), top.Penalty(), 
						q.size(), m_countInvalidStates, usedMem);
				s_log.printf("ShortCut %,d\n\n", shortcutCount);
				s_log.flush();
			}
			
			if (top.IsFullyConnected())
				return new Result(top.MachinesUsed());
			
			top.AddChildren(q);
		}

		assert false;
		return null;
	}
	
	public void Run() {
		s_log.printf("%d machines and %d compounds\n", m_machines.size(), m_compounds.length);
		
		for (Compound c : m_compounds)
			m_fullMask = m_fullMask | c.Mask();
		
		Result result = BreathFirst();
		
		System.out.println(result.TotalPrice());
		System.out.println(result);

		int[][] connectivity = FloydWarshall(result.RequiredMachines());
		
		for (int[] row : connectivity) {
			s_log.println(Arrays.toString(row));
		}
		
		assert IsConnected(connectivity);
	}
	
	public int[][] FloydWarshall(Machine[] machines) {

		int n = m_compounds.length;
		
		int[][] result = new int[n][];
		for (int i = 0; i < n; i++) {
			result[i] = new int[n];
			Arrays.fill(result[i], Integer.MAX_VALUE / 2);
			result[i][i] = 0;
		}
		
		for (Machine m : machines) {
			result[m.Input().Index()][m.Output().Index()] = m.Price();
		}

		for (int k = 0; k < n; k++)
			for (int i = 0; i < n; i++)
				for (int j = 0; j < n; j++) {
					int bestByK = result[i][k] + result[k][j];
					result[i][j] = Math.min(result[i][j], bestByK);
				}
		
		return result;
	}
	
	private Compound FindCompound(String name, HashMap<String, Compound> nameToCompound) {
		Compound result = nameToCompound.get(name);
		if (result == null) {
			result = new Compound(name, nameToCompound.size());
			nameToCompound.put(name, result);
		}
		return result;
	}
	
	private Compound[] m_compounds;
	private HashMap<String, Machine> m_machines;
	
	private long m_fullMask;
	
	// If you are up to Compound i, records which Compounds have consumers in the remaining producers 
	private long[] m_remainingConsumers;
	private int[] m_minProducerCost;
	
	private int m_countInvalidStates;
	
	private static PrintWriter s_log;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		assert args.length == 1;
		
		try
		{
			s_log = new PrintWriter("Log.txt");
			
			FaceBullMain inst = new FaceBullMain(args[0]);
			
			inst.Run();
			
			s_log.close();
		}
		catch (Exception e)
		{
			s_log.close();
			throw new RuntimeException(e);
		}
	}
}
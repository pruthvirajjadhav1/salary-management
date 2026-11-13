package com.acme.salary.seed;

import java.util.List;
import java.util.Map;

/** First and last names by country, so a seeded employee in Pune is not called "Hans Müller". */
final class NamePools {

	record NamePool(List<String> firstNames, List<String> lastNames) {
	}

	private static final NamePool ENGLISH_SPEAKING = new NamePool(List.of("James", "Olivia", "Liam", "Emma", "Noah",
			"Ava", "William", "Sophia", "Benjamin", "Isabella", "Lucas", "Mia", "Henry", "Charlotte", "Alexander",
			"Amelia", "Daniel", "Harper", "Michael", "Evelyn", "Ethan", "Abigail", "Jacob", "Emily", "Samuel", "Grace",
			"David", "Chloe", "Joseph", "Hannah", "Aisha", "Mateo", "Wei", "Priya", "Omar", "Sofia", "Kenji", "Zara",
			"Diego", "Nia"),
			List.of("Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez",
					"Martinez", "Wilson", "Anderson", "Taylor", "Thomas", "Moore", "Jackson", "Martin", "Lee",
					"Thompson", "White", "Harris", "Clark", "Lewis", "Robinson", "Walker", "Young", "Allen", "King",
					"Wright", "Scott", "Nguyen", "Patel", "Kim", "Chen", "O'Brien", "Murphy", "Campbell", "Mitchell",
					"Roberts", "Carter"));

	private static final NamePool INDIAN = new NamePool(List.of("Aarav", "Vivaan", "Aditya", "Arjun", "Sai",
			"Reyansh", "Krishna", "Ishaan", "Rohan", "Vikram", "Rahul", "Karan", "Siddharth", "Pranav", "Nikhil",
			"Ananya", "Diya", "Aadhya", "Saanvi", "Isha", "Priya", "Kavya", "Meera", "Neha", "Pooja", "Riya", "Shreya",
			"Sneha", "Tanvi", "Divya"),
			List.of("Sharma", "Verma", "Gupta", "Patel", "Singh", "Kumar", "Reddy", "Rao", "Iyer", "Nair", "Menon",
					"Pillai", "Joshi", "Kulkarni", "Deshpande", "Jadhav", "Patil", "Mehta", "Shah", "Desai", "Bose",
					"Chatterjee", "Banerjee", "Mukherjee", "Das", "Malhotra", "Kapoor", "Agarwal", "Chopra", "Bhat"));

	private static final NamePool GERMAN = new NamePool(List.of("Lukas", "Leon", "Finn", "Jonas", "Paul", "Felix",
			"Maximilian", "Elias", "Noah", "Ben", "Moritz", "Jan", "Tobias", "Niklas", "Julian", "Emma", "Mia",
			"Hannah", "Lena", "Lea", "Anna", "Sophie", "Marie", "Laura", "Katharina", "Julia", "Clara", "Johanna",
			"Greta", "Frieda"),
			List.of("Müller", "Schmidt", "Schneider", "Fischer", "Weber", "Meyer", "Wagner", "Becker", "Schulz",
					"Hoffmann", "Schäfer", "Koch", "Bauer", "Richter", "Klein", "Wolf", "Schröder", "Neumann",
					"Schwarz", "Zimmermann", "Braun", "Krüger", "Hofmann", "Hartmann", "Lange", "Schmitt", "Werner",
					"Krause", "Meier", "Lehmann"));

	private static final NamePool BRAZILIAN = new NamePool(List.of("Miguel", "Arthur", "Gael", "Heitor", "Theo",
			"Davi", "Gabriel", "Bernardo", "Samuel", "João", "Pedro", "Lucas", "Rafael", "Gustavo", "Felipe", "Helena",
			"Alice", "Laura", "Maria", "Valentina", "Heloísa", "Luiza", "Júlia", "Beatriz", "Mariana", "Ana", "Camila",
			"Larissa", "Fernanda", "Letícia"),
			List.of("Silva", "Santos", "Oliveira", "Souza", "Rodrigues", "Ferreira", "Alves", "Pereira", "Lima",
					"Gomes", "Costa", "Ribeiro", "Martins", "Carvalho", "Almeida", "Lopes", "Soares", "Fernandes",
					"Vieira", "Barbosa", "Rocha", "Dias", "Nascimento", "Andrade", "Moreira", "Nunes", "Marques",
					"Machado", "Mendes", "Freitas"));

	private static final NamePool SINGAPOREAN = new NamePool(List.of("Wei Ling", "Jun Jie", "Xin Yi", "Kai Wen",
			"Hui Min", "Zhi Hao", "Jia Hui", "Wei Jie", "Mei Ling", "Jun Wei", "Siti", "Nurul", "Muhammad", "Ahmad",
			"Farah", "Hafiz", "Aisyah", "Arjun", "Priya", "Ravi", "Ethan", "Chloe", "Ryan", "Rachel", "Marcus",
			"Natalie", "Darren", "Joanne", "Shawn", "Valerie"),
			List.of("Tan", "Lim", "Lee", "Ng", "Ong", "Wong", "Goh", "Chua", "Chan", "Koh", "Teo", "Ang", "Yeo", "Tay",
					"Ho", "Low", "Toh", "Sim", "Chong", "Chia", "Rahman", "Ismail", "Abdullah", "Hassan", "Kumar",
					"Pillai", "Nair", "Chandran", "Raj", "Fernandez"));

	private static final Map<String, NamePool> BY_COUNTRY = Map.of("US", ENGLISH_SPEAKING, "GB", ENGLISH_SPEAKING,
			"CA", ENGLISH_SPEAKING, "AU", ENGLISH_SPEAKING, "IN", INDIAN, "DE", GERMAN, "BR", BRAZILIAN, "SG",
			SINGAPOREAN);

	private NamePools() {
	}

	static NamePool forCountry(String countryCode) {
		return BY_COUNTRY.getOrDefault(countryCode, ENGLISH_SPEAKING);
	}

}

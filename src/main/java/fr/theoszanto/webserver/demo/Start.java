package fr.theoszanto.webserver.demo;

import fr.theoszanto.webserver.WebServer;
import fr.theoszanto.webserver.api.HtmlTemplate;
import fr.theoszanto.webserver.handler.IntermediateHandler;
import fr.theoszanto.webserver.routing.RouteBuilder;

import java.util.Scanner;

/**
 * Start an example web server.
 * 
 * @author	indyteo
 */
public class Start {
	public static void main(String[] args) {
		WebServer ws = new WebServer(8080, "./files");
		Scanner sc = new Scanner(System.in);

		//GlobalHandler.enable(ws);
		HtmlTemplate.loadTemplates(ws, "template");
		ws.getRouter()
				.registerRoute(new RouteBuilder()
						.setName("Test")
						.setRoute("/test/{type}")
						//.setMethod(HttpMethod.GET)
						.setHandler((request, response) -> {
							String type = request.getRouteParam("type");
							if (type == null) return;
							switch (type) {
							case "escape":
								response.sendEscaped("<h1 title=\"Hey\">'Coucou' &copy;</h1>").end();
								break;
							case "json":
								System.out.println(request.getJsonParams());
								JsonExample jsonExample = response.loadJson("example.json", JsonExample.class);
								jsonExample.setNumber(jsonExample.getNumber() + 1);
								response.saveJson("example.json", jsonExample);
								jsonExample.setPerson(new Person("Bar", 34));
								response.sendJson(jsonExample);
								break;
							case "template":
								response.sendTemplate(HtmlTemplate.getTemplate("template")
										.placeholder("title", "Hello World!")
										.placeholder("text", "Lorem ispum j'ai la flemme")
										.placeholder("escaped", "Never shown")
										.placeholder("with special {name}", 7));
								break;
							}
						})
						.setStrict(true)
						.buildRoute())
				.registerIntermediateRoute(new RouteBuilder()
						.setName("Logger")
						.setIntermediateHandler(IntermediateHandler.LOG)
						.buildIntermediateRoute());

		ws.logDebugInfo();
		ws.getRouter().logDebugInfo();

		System.out.println("Press enter to stop the server...");
		sc.nextLine();
		ws.close();
		sc.close();
	}

	private static class JsonExample {
		private String string;
		private int number;
		private Person person;

		public JsonExample(String string, int number, Person person) {
			this.string = string;
			this.number = number;
			this.person = person;
		}

		public String getString() {
			return string;
		}

		public void setString(String string) {
			this.string = string;
		}

		public int getNumber() {
			return number;
		}

		public void setNumber(int number) {
			this.number = number;
		}

		public Person getPerson() {
			return person;
		}

		public void setPerson(Person person) {
			this.person = person;
		}
	}

	private static class Person {
		private String name;
		private int age;

		public Person(String name, int age) {
			this.name = name;
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}
	}
}

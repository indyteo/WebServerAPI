package fr.theoszanto.webserver.demo;

import fr.theoszanto.webserver.WebServer;
import fr.theoszanto.webserver.api.Cookie;
import fr.theoszanto.webserver.api.HtmlTemplate;
import fr.theoszanto.webserver.api.Session;
import fr.theoszanto.webserver.handling.IntermediateHandler;
import fr.theoszanto.webserver.routing.RouteBuilder;
import fr.theoszanto.webserver.utils.JsonUtils;

import java.util.Scanner;

/**
 * Start an example web server.
 * 
 * @author	indyteo
 */
public class Start {
	public static void main(String[] args) {
		WebServer ws = new WebServer(8080, "./files", "./sessions");
		Scanner sc = new Scanner(System.in);

		//GlobalHandler.enable(ws);
		ws.loadTemplates("template").getRouter()
				.registerRoute(new RouteBuilder()
						.setName("Test")
						.setRoute("/test/{type}")
						//.setMethod(HttpMethod.GET)
						.setHandler((request, response) -> {
							String type = request.getRouteParam("type");
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
							case "cookie":
								response.cookie(new Cookie.Builder()
										.setName("person")
										.setValue("toto")
										.setSameSite(Cookie.SameSitePolicy.LAX)
										.setHttpOnly(true)
										.build())
										.cookie(new Cookie.Builder()
										.setName("id")
										.setValue("8")
										.setSameSite(Cookie.SameSitePolicy.LAX)
										.setHttpOnly(true)
										.build())
										.end();
								break;
							case "cookie-delete":
								response.deleteCookie("id").end();
								break;
							case "session":
								Session session = request.getSession();
								if (session.isInit()) {
									Person data = JsonUtils.GSON.fromJson(session.get("person"), Person.class);
									System.out.println("Session data: " + data);
								}
								session.set("person", JsonUtils.GSON.toJson(new Person("Toto", 7)));
								System.out.println("Session ID: " + session.getId());
								response.end();
								break;
							case "session-destroy":
								request.getSession().destroy();
								response.end();
								break;
							case "session-regen":
								request.getSession().regenerateId();
								response.end();
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

		@Override
		public String toString() {
			return "Person{" +
					"name='" + name + '\'' +
					", age=" + age +
					'}';
		}
	}
}

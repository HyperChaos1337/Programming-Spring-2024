package com.example.LP_2_Demo;

import com.google.zxing.WriterException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@Controller
public class Lp2DemoApplication {

	SQLClient client = new SQLClient();

	@GetMapping("/")
	public String showMainMenu() {
		return "main";
	}

	@GetMapping("/applicant")
	public String showApplicantMenu() {
		return "applicant";
	}

	@GetMapping("/chancery")
	public String showChanceryMenu() {
		return "chancery";
	}

	@GetMapping("/manager")
	public String showManagerMenu() {
		return "manager";
	}

	@GetMapping("/office1")
	public String showOffice1Menu() {
		return "office1";
	}

	@GetMapping("/office2")
	public String showOffice2Menu() {
		return "office2";
	}

	@PostMapping("/applicant/submit")
	public String submitApplicantData(@RequestParam("field1") String field1,
									  @RequestParam("field2") String field2,
									  @RequestParam("field3") String field3,
									  @RequestParam("field4") String field4,
									  @RequestParam("field5") String field5,
									  Model model) throws SQLException {
		// TODO: Обработать данные, например, сохранить их в БД
		client.insertData(client.FIRST_HOST, client.REQUESTS, IDGenerator.generateUniqueID(),
				field1, field2, field3, field4, field5, "На рассмотрении",
				null, "По усмотрению руководителя");
		model.addAttribute("message", "Данные успешно отправлены!");
		return "applicant";
	}


	@GetMapping("/chancery/print_created")
	public String printCreatedData(Model model) {

		List<String> dataToDisplay = new ArrayList<>();
		int row = 0, col = 0;
		String[] receivedData = client.getEachID(client.FIRST_HOST,
				client.REQUESTS, false).split("; ");
		for (String data : receivedData) {
			dataToDisplay.add(data);
			col++;
			if (col == 5) {
				col = 0;
				row++;
			}
		}
		model.addAttribute("dataToDisplay", dataToDisplay);
		model.addAttribute("receivedData", receivedData);
		return "print_created";
	}

	@GetMapping("/office1/encoded_created")
	public String printEncodedData(Model model) {

		List<String> dataToDisplay = new ArrayList<>();
		int row = 0, col = 0;
		String[] receivedData = client.getEachID(client.FIRST_HOST,
				client.REQUESTS, false).split("; ");
		for (String data : receivedData) {
			String filename = "request" + data + ".png";
			File file = new File(filename);
			if (file.exists()) {
				dataToDisplay.add(data);
				col++;
				if (col == 5) {
					col = 0;
					row++;
				}
			}
		}
		model.addAttribute("dataToDisplay", dataToDisplay);
		model.addAttribute("receivedData", receivedData);
		return "encoded_created";
	}

	@GetMapping("/manager/print_decoded")
	public String printDecodedData(Model model) {

		List<String> dataToDisplay = new ArrayList<>();
		int row = 0, col = 0;
		String[] receivedData = client.getEachID(client.FIRST_HOST,
				client.PRINTED_REQUESTS, false).split("; ");
		for (String data : receivedData) {
			dataToDisplay.add(data);
			col++;
			if (col == 5) {
				col = 0;
				row++;
			}
		}
		model.addAttribute("dataToDisplay", dataToDisplay);
		model.addAttribute("receivedData", receivedData);
		return "print_decoded";
	}

	@GetMapping("/chancery/print_created/created_data")
	public String getDataFromID(
			@RequestParam("id") int id,
			Model model) throws SQLException, IOException, WriterException {

		String data = client.getCurrentID(client.FIRST_HOST,
				client.REQUESTS, id); // Получаем данные из БД

		String[] entries = data.split("; ");
		List<String[]> dataToDisplay = new ArrayList<>(); // Создаем список для отображения в HTML

        for (String entry : entries) {
            String[] parts = entry.split(": ");
            if (parts.length == 2) {
                dataToDisplay.add(parts);
            }
        }
		QRCodeTool.generateQRCode(data, "request" + String.valueOf(id) + ".png");
		model.addAttribute("dataToDisplay", dataToDisplay);
		return "created_data"; // Имя HTML-шаблона для отображения результата
	}

	@PostMapping("/office1/encoded_created")
	public String decodeQr(
			@RequestParam("id") int id,
			Model model) {

		try {
			// Декодируем QR-код
			String data = QRCodeTool.decodeQRCode("request" + String.valueOf(id) + ".png");

			// Передаем декодированный текст в модель
			model.addAttribute("decodedText", data);

			return "encoded_created"; // Перенаправляем на ту же страницу
		} catch (Exception e) {
			// Обработка ошибок
			model.addAttribute("error", "Ошибка декодирования QR-кода: " + e.getMessage());
			return "encoded_created"; // Возвращаем на ту же страницу в случае ошибки
		}
	}

	@PostMapping("/manager/print_decoded")
	public String printDecoded(
			@RequestParam("id") int id,
			Model model) {

		try {
			// Получаем строку данных (в вашем реальном коде используйте источник данных)
			String dataString = getDecodedData(id);

			// Разделяем строку на пары ключ-значение
			Map<String, String> data = parseDataString(dataString);

			// Передаем данные в модель
			model.addAttribute("data", data);
			model.addAttribute("id", id);
			return "print_decoded"; // Перенаправляем на ту же страницу
		} catch (Exception e) {
			// Обработка ошибок
			model.addAttribute("error", "Ошибка: " + e.getMessage());
			return "print_decoded"; // Возвращаем на ту же страницу в случае ошибки
		}
	}

	@PostMapping("/manager/update_data")
	public String sendResolution(
			@RequestParam("resolution") String resolution,
			@RequestParam("status") boolean status,
			@RequestParam("info") String info,
			@RequestParam("idValue") int idValue,
			Model model) {

		try {
			// Вызов функции updateData
			client.updateData(client.FIRST_HOST, client.PRINTED_REQUESTS,
					resolution, status, info, idValue);

			// Обработка успешного обновления
			model.addAttribute("success", "Данные успешно обновлены!");

			return "redirect:/manager"; // Перенаправление в /manager
		} catch (Exception e) {
			// Обработка ошибок
			model.addAttribute("error", "Ошибка обновления данных: " + e.getMessage());
			return "redirect:/manager/print_decoded"; // Возврат на print_decoded
		}
	}

	@PostMapping("/office1/encoded_created/send")
	public String sendData(
			@RequestParam("decodedText") String dataToSend,
			Model model) throws SQLException {

		// Вызов функции prepareToSend
		prepareToSend(client.FIRST_HOST, dataToSend, client.PRINTED_REQUESTS);

		// Перенаправляем на страницу успеха (или на ту же страницу)
		return "redirect:/office1"; // Измените путь на нужный
	}

	// Функция prepareToSend, адаптированная для Spring
	private void prepareToSend(String dataBase, String dataToSend, String tableName) throws SQLException {
		ArrayList<String> data = new ArrayList<>();
		String[] pairs = dataToSend.split("; ");
		for (String current : pairs) {
			String[] entries = current.split(": ");
			data.add(entries[1]);
		}

		// Проверка на "null"
		Boolean status = null;
		if (!data.get(7).equalsIgnoreCase("null")) {
			status = Boolean.valueOf(data.get(7));
		}

		// Вставка данных в таблицу (предполагаем, что у вас есть клиент для БД)
		// Измените этот код, чтобы использовать ваш клиент
		client.insertData(dataBase, tableName, Integer.parseInt(data.get(0)),
		        data.get(1), data.get(2), data.get(3), data.get(4),
		        data.get(5), data.get(6), status, data.get(8));
	}

	private String getDecodedData(int id) throws SQLException {
		// Здесь вы должны получить строку данных из вашего источника данных
		// Например, из базы данных или файла
		return client.getCurrentID(client.FIRST_HOST, client.PRINTED_REQUESTS, id);
	}

	// Метод для разбора строки на пары ключ-значение
	private Map<String, String> parseDataString(String dataString) {
		Map<String, String> data = new HashMap<>();
		String[] pairs = dataString.split("; ");
		for (String current : pairs) {
			String[] entries = current.split(": ");
			data.put(entries[0].trim(), entries[1].trim());
		}
		return data;
	}

	@PostMapping("/applicant/submit2")
	public String submitApplicantData2(@RequestParam("field6") String field6,
									   Model model) {
		// TODO: Обработать данные, например, сохранить их в БД
		model.addAttribute("message", "Данные успешно отправлены!");
		return "applicant";
	}

	public static void main(String[] args) {
		SpringApplication.run(Lp2DemoApplication.class, args);
	}
}


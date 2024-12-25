package com.blize.command;

import com.blize.document.Message;
import com.blize.dto.request.RegisterRequestDTO;
import com.blize.repository.UserRepository;
import com.blize.responder.StoryResponder;
import com.blize.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.*;

@Component
public class DummyDataCommandlineRunner implements CommandLineRunner {

    private RestTemplate restTemplate = new RestTemplate();
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private StoryResponder storyResponder;

    @Override
    public void run(String... args) throws Exception {

        Set<String> argList = Set.of(args);
        if (!argList.isEmpty() && argList.contains("Fill-Dummy-Data")) {
            System.out.println("DummyDataCommandlineRunner...");
            System.out.println("insertDefaultUsers...");
            this.insertDefaultUsers();
            System.out.println("insertMarvelUsers...");
            this.insertMarvelUsers();
            System.out.println("insertMongoMessages...");
            this.insertMongoMessages();
            System.out.println("insertMongoStories...");
            this.insertMongoStories();
            System.out.println("end DummyDataCommandlineRunner");
        }
    }

    public Map<String, Map<String, Object>> getMarvelCharacters(int offset, int limit) {
        var privateKey = "3d037b2391ebb04e17ff86ee55257334d355f2de";
        var publicKey = "88407e08345941124a7db1309d680edb";
        String url = UriComponentsBuilder
                .fromHttpUrl("http://gateway.marvel.com/v1/public/characters")
                .queryParam("ts", System.currentTimeMillis() / 1000)
                .queryParam("apikey", publicKey)
                .queryParam("hash", DigestUtils.md5DigestAsHex(((System.currentTimeMillis() / 1000) + privateKey + publicKey ).getBytes()))
                .queryParam("limit", limit)
                .queryParam("offset", offset)
                .toUriString();

        return restTemplate.getForObject(url, Map.class);
    }

    private void insertDefaultUsers() {
        List<Map<String, String>> users = Arrays.asList(
                Map.of("firstName", "Erdal", "lastName", "CEYLAN", "username", "erdalceylan", "email", "erdalceylan@example.com"),
                Map.of("firstName", "John", "lastName", "Smith", "username", "johnsmit", "email", "johnsmit@example.com"),
                Map.of("firstName", "Cüneyt", "lastName", "Özdemir", "username", "cuneytozdemir", "email", "cuneytozdemir@example.com"),
                Map.of("firstName", "Tijen", "lastName", "Karakaş", "username", "tijenkarakas", "email", "tijen@example.com"),
                Map.of("firstName", "Hyko", "lastName", "Cepkin", "username", "hayko", "email", "hayko@example.com"),
                Map.of("firstName", "Ryan", "lastName", "GOSLING", "username", "ryangosling", "email", "ryangosling@example.com"),
                Map.of("firstName", "Hugh", "lastName", "Jackman", "username", "hughjackman", "email", "hughjackman@example.com"),
                Map.of("firstName", "Max", "lastName", "Plank", "username", "maxplank", "email", "maxplank@example.com"),
                Map.of("firstName", "Maria", "lastName", "Curie", "username", "mariacurie", "email", "mariacurie@example.com"),
                Map.of("firstName", "Margot", "lastName", "Robbie", "username", "margotrobbie", "email", "margotrobbie@example.com"),
                Map.of("firstName", "Erwin", "lastName", "Schrödinger", "username", "erwinschrodinger", "email", "erwinschrodinger@example.com"),
                Map.of("firstName", "Platon", "lastName", "Philosopher", "username", "platon", "email", "platon@example.com"),
                Map.of("firstName", "Aristoteles", "lastName", "Philosopher", "username", "aristoteles", "email", "aristoteles@example.com"),
                Map.of("firstName", "Sokrates", "lastName", "Philosopher", "username", "sokrates", "email", "sokrates@example.com"),
                Map.of("firstName", "Archimedes", "lastName", "Physicist", "username", "archimedes", "email", "archimedes@example.com")
        );

        for (Map<String, String> item : users) {
            RegisterRequestDTO registerDTO = new RegisterRequestDTO();
            registerDTO.setFirstName(item.get("firstName"));
            registerDTO.setLastName(item.get("lastName"));
            registerDTO.setUsername(item.get("username"));
            registerDTO.setEmail(item.get("email"));
            registerDTO.setPassword("123456");
            userService.register(registerDTO, null);
        }
    }

    private void insertMarvelUsers() {
        try {

            Map<String, Map<String, Object>> characters = getMarvelCharacters(1500, 100);

            // Marvel API'den gelen veriler üzerinden kullanıcı ekleme
            for (Map<String, Object> character : (List<Map<String, Object>>) characters.get("data").get("results")) {
                String username = ((String) character.get("name")).replaceAll("[^a-zA-Z0-9]", "");
                var path = ((Map<String, String>) character.get("thumbnail")).get("path");
                String file = null;
                if (path != null && !path.contains("image_not_available")) {
                    file = path + "." + ((Map<String, String>) character.get("thumbnail")).get("extension");
                    System.out.println(file);
                    var fileInfo = userService.uploadImage(ImageIO.read(new URL(file)));
                    file = fileInfo.toString();
                }

                RegisterRequestDTO registerDTO = new RegisterRequestDTO();
                registerDTO.setFirstName((String) character.get("name"));
                registerDTO.setLastName("Marvel");
                registerDTO.setUsername(username.toLowerCase(Locale.getDefault()));
                registerDTO.setEmail(username + "@marvel.com");
                registerDTO.setPassword("123456");

                userService.register(registerDTO, file);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertMongoMessages(){
        var users = userRepository.findAll();
        Random random = new Random();
        final String text = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.Why do we use it? It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using 'Content here, content here', making it look like readable English. Many desktop publishing packages and web page editors now use Lorem Ipsum as their default model text, and a search for 'lorem ipsum' will uncover many web sites still in their infancy. Various versions have evolved over the years, sometimes by accident, sometimes on purpose (injected humour and the like).Where does it come from? Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old. Richard McClintock, a Latin professor at Hampden-Sydney College in Virginia, looked up one of the more obscure Latin words, consectetur, from a Lorem Ipsum passage, and going through the cites of the word in classical literature, discovered the undoubtable source. Lorem Ipsum comes from sections 1.10.32 and 1.10.33 of de Finibus Bonorum et Malorum (The Extremes of Good and Evil) by Cicero, written in 45 BC. This book is a treatise on the theory of ethics, very popular during the Renaissance. The first line of Lorem Ipsum, Lorem ipsum dolor sit amet.., comes from a line in section 1.10.32. The standard chunk of Lorem Ipsum used since the 1500s is reproduced below for those interested. Sections 1.10.32 and 1.10.33 from de Finibus Bonorum et Malorum by Cicero are also reproduced in their exact original form, accompanied by English versions from the 1914 translation by H. Rackham. Where can I get some? There are are many variations of passages of Lorem Ipsum available, but the majority have suffered alteration in some form, by injected humour, or randomised words which don't look even slightly believable. If you are going to use a passage of Lorem Ipsum, you need to be sure there isn't anything embarrassing hidden in the middle of text. All the Lorem Ipsum generators on the Internet tend to repeat predefined chunks as necessary, making this the first true generator on the Internet. It uses a dictionary of over 200 Latin words, combined with a handful of model sentence structures, to generate Lorem Ipsum which looks reasonable. The generated Lorem Ipsum is therefore always free from repetition, injected humour, or non-characteristic words etc.";

        for (int i = 0; i < 500000; i++) {
            var u1 = users.get(random.nextInt(users.size()));
            var u2 = users.get(random.nextInt(users.size()));

            if (u1.getId() != u2.getId()) {
                int start = random.nextInt(0,text.length() - 100);
                int end = random.nextInt(start+ 20, text.length());
                String messageText = text.substring(start, random.nextInt(start + 5, end));


                Message message = new Message();
                message.setFrom(u2.getId());
                message.setTo(u1.getId());
                message.setText(messageText);
                message.setRead(false);
                message.setDate(OffsetDateTime.now().minusSeconds(i));

                mongoTemplate.save(message);
                System.out.println(u1.getId() + " " + u2.getId() + " " + messageText+" index : "+i);
            }
        }
    }

    private void insertMongoStories() throws IOException {
        var users = userRepository.findAll();
        Random random = new Random();
        var url = new URL("https://picsum.photos/600/1050");
        for (var user : users) {
            for (int i =0; i< random.nextInt(2,7); i++) {

                Resource resource = new UrlResource(url);
                InputStreamResource inputStreamResource = new InputStreamResource(resource.getInputStream());
                storyResponder.add(user, inputStreamResource);
            }
        }
    }

}

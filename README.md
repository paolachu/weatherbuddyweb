# BuddyWeather

## Descrição do Projeto
O projeto BuddyWeather é uma aplicação web desenvolvida com o objetivo de fornecer informações climáticas atuais e previsões de tempo de forma prática e acessível. Utilizando a API do OpenWeatherMap, a aplicação permite que os usuários obtenham dados detalhados sobre o clima de qualquer cidade do mundo ou com base em suas coordenadas geográficas.

A aplicação é composta por um backend desenvolvido em Java com o framework Spring Boot e um frontend dinâmico utilizando HTML, CSS e JavaScript. A interface do usuário é projetada para ser intuitiva e responsiva, facilitando a navegação e a obtenção de informações relevantes sobre o clima.

## Funcionalidades
- Exibir condições climáticas atuais.
- Fornecer previsões meteorológicas para os próximos cinco dias.
- Permitir a pesquisa de informações climáticas por cidade ou coordenadas geográficas.
- Interface de usuário responsiva e amigável.


## Tecnologias Utilizadas
- Java
- Spring Boot
- HTML
- CSS
- JavaScript
- API do OpenWeatherMap

## Configuração e Execução

### Pré-requisitos
- Java 11 ou superior
- Maven

### Passos para Configuração
1. Clone o repositório:
    ```sh
    git clone https://github.com/paolachu/weatherbuddyweb.git
    ```
2. Navegue até o diretório do projeto:
    ```sh
    cd weatherbuddyweb
    ```
3. Compile e construa o projeto:
    ```sh
    mvn clean install
    ```
4. Execute a aplicação:
    ```sh
    mvn spring-boot:run
    ```

### Acesso à Aplicação
- A aplicação estará disponível em `http://localhost:8080`.

## Exemplos de Uso

### Consulta por Cidade
- Insira o nome da cidade no campo de pesquisa e clique em "Pesquisar".

### Consulta por Coordenadas Geográficas
- Permita o acesso à localização pelo navegador para obter dados climáticos com base na sua localização atual.

## Contribuição
Se você quiser contribuir com este projeto, por favor siga os passos abaixo:
1. Fork este repositório.
2. Crie uma branch para sua feature (`git checkout -b minha-feature`).
3. Faça commit das suas alterações (`git commit -m 'Adiciona minha feature'`).
4. Faça push para a branch (`git push origin minha-feature`).
5. Abra um Pull Request.

## Licença
Este projeto está licenciado sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## Agradecimentos
Agradecemos a todos que contribuíram para este projeto. Um agradecimento especial à equipe que colaborou e compartilhou seus conhecimentos durante o desenvolvimento.

--
Este é um projeto iniciante para fins acadêmicos.


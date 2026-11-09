# Rules

- When designing services, use vertical slicing over horizontal slicing. Helps with maintainability as a service's dependencies are contained within one directory
    ```
    // BAD 
    controllers/
        UserController.java
        PlayerController.java
    handlers/
        UserHandler.java
        PlayerController.java
    entities/
        PlayerEntities.java
        UserEntities.java

    // GOOD
    player/
        PlayerController.java 
        PlayerService.java
        PlayerRepository.java
    user/
        UserController.java 
        UserRepository.java 
        UserService.java 
    ```

    Each service must have the following files
    - NameController.java       - Handles routing for features 
    - NameService.java          - Business logic
    - NameRepository.java       - Handles database connections 

    If requests and responses get too big, you may opt to have separate `NameRequest.java` and `NameResponse.java` if needed 

    ```
    Ex.
    player/
        PlayerController.java 
        PlayerService.java
        PlayerRepository.java
        PlayerResponse.java
    user/
        UserController.java 
        UserRepository.java 
        UserService.java 
        UserRequest.java 
    ```

    If several features write to the same rows, those invariants belong to the domain. They should get a directory of their own 
    ```
    Ex.

    player/
        domain/
            Player.Java 
            PlayerRepository.java 
        PlayerController.java
        PlayerService.java
        
    ```
- Use Slf4j logging. It is important that the developers know what is happening with the backend
- Commenting is a good practice unless abused. In general:
    - Comments should explain what a function does. 
    - Comments should be at most 1 sentence. If a comment needs to be explained using 2 or more sentences, that means that the code is doing too much and needs to be refactored
      
      Exceptions to this rule: Genuinely hard to explain stuff (i.e. pseudocode for an algorithm, mathematical expressions and proofs, compliance requirements, etc.) or for warnings that is difficult to explain within one sentence
    - Write descriptive code instead of relying on comments. Code should be self-documenting and easily read

# 3rd Party API (Google GSheets) Use Tutorial - Temporal.io based Worker

## What we will create?

This tutorial will guide us in building a typical Customer Onboarding process using a Temporal.io worker workflow (*when we apply the Workflow as Code paradigm*), connecting to a 3rd Party API (**Google Apps Script**) to receive customer's data, such as Client name and a Project name, via KuFlow User Interface and call a Script which creates in our Google Drive the corresponding folder structure.

## Prerequisites

Before starting your Workflow for the first time, you must register in [KuFlow (app.kuflow.com)](https://app.kuflow.com/). After familiarizing yourself with the user interface by navigating through the menus or visiting our [Youtube channel](https://www.youtube.com/channel/UCXoRtHICa86YfX8P_wu1f6Q) with many videos that will help you in this task, you are ready to perform the necessary configurations for our Worker. To do so, click on the `Management` menu.

### Create the Credentials

#### 3rd Party API (Google Apps Script) Credentials

To use this 3rd-party application, you must create a Google Dev Project and credentials following the [Google Developer Platform](https://developers.google.com/apps-script/api/quickstart/java?hl=es-419) quickstart guide.

#### Create the credentials for the Worker​

We will configure an `APPLICATION` that will provide us with the necessary credentials so that our Worker (written in Java and located in your own machine) can interface with KuFlow.

Go to the `Settings > Applications` menu and click on `Add application`. We establish the name we want and save. Next, you will get the first data needed to configure our Worker.

- **Identifier**: Unique identifier of the application. For this tutorial: *myApp*
  - Later in this tutorial, we will configure it in the `kuflow.api.client-id` property of our example.
- **Token**: Password for the application.
  - Later in this tutorial, we will configure it in the `kuflow.api.client-secret` property of our example.
- **Namespace**: Temporal's namespace.
  - Later in this tutorial, we will configure it in the `application.temporal.namespace` property of our example.

Next, we will proceed to create the certificates that will serve us to configure the Mutual TLS with which our Worker will perform the authentication against Temporal. To do this we click on "Add certificate", set the name we want, and choose the `PKCS8` type for the encryption of the private key. This is important since the example code in this tutorial works with this encoding. We will get the following:

- **Certificate**: It is the public part that is presented in the *mTLS* connection.
  - Later in this tutorial, we will configure it in the `application.temporal.mutual-tls.cert-data` property of our example.
- **Private Key**: It is the private key for *mTLS*.
  - Later in this tutorial, we will configure it in the `application.temporal.mutual-tls.key-data` property of our example.

It is also necessary to indicate the CA certificate, which is the root certificate with which all certificates are issued. It is a public certificate and for convenience you can find it in the same `Application` screen, under the name of *CA Certificate*. This certificate will always be the same between workers.

- **CA Certificate**: Root certificate with which all certificates (client and server certificates) are issued.
  - Later in this tutorial, we will configure it in the `kuflow.activity.kuflow.key-data` property of our example.

Finally, you get something like:

<div class="text--center">

![](/img/tutorial/java-APItutorial2/TUT03-01-App.png)

</div>

## Preparing Scenario

### Create the Apps Script

Create a new Apps Script in a GDrive folder and insert your code, if you don't have it we provide a simple one:

	function createFolder(client, project, year) {
	  
	  //Define Variables
	  var client;
	  var project;
	  var year;
	
	  // Destination Folder ID
	  var destFolder = DriveApp.getFolderById("DESTINATION_FOLDER_ID"); 
	  Logger.log(destFolder.getName());
	  
	  //Create Folders
	  try {
	    var yearFolder = destFolder.createFolder(year);
	    var clientFolder = yearFolder.createFolder(client);
	    clientFolder.createFolder(project);
	    Logger.log("Folders created correctly");
	  } catch (error) {
	    Logger.log("An error has occurred: " + error.toString());
	  }
	}

**NOTE:** For simplification purposes, we have not added existing folder validations.

### Create the process definition

We need to create the definition of the process that will execute our workflow. In this section, we will configure the KuFlow tasks of which it is made up as well as the information necessary to complete said tasks, the process access rules (i.e. *RBAC*), as well as another series of information. To do this we go to the `Setting > Processes` menu and create a new process.

Complete *Process Definition* with the (*recommended*) following data:

- **Process name**
	- New Customer OnBoarding
- **Description**
	- Free text description about the Workflow.
- **Workflow**
	- **Workflow Engine**
		- *KuFlow Engine*, because we are designing a Temporal-based Worker.
  - **Workflow Application**
      - myApp, the application to which our Worker will connect to.
	- **Task queue**
		- The name of the Temporal queue where the KuFlow tasks will be set. You can choose any name, later you will set this same name in the appropriate configuration in your Worker. For this tutorial: *NCOBQueue*.
	- **Type**
		- It must match the name of the Java interface of the Workflow. For this tutorial, *NCOBWorker* is the name you should type in this input.
- **Permissions**
	- At least one user or group of users must have the role of `INITIATOR` to instantiate the process through the application. In this tutorial, we will allow the *“Default Group”* from this organization.

Finally, you get something like:

<div class="text--center">

![](/img/tutorial/java-APItutorial2/TUT03-03-Process.png)

</div>

We will define one **Task Definition** in the process as follows:

- Task one **"Data Entry Form"**
	- **Description:** Free text description about the Task.
	- **Code:** NCOB_FRM
	- **Candidates:** Default Group (*in this tutorial, we will allow all users from this organization to fill up the application form*)
	- **Elements:**
		- **Name:** Client Name
			- **Description:** Free text description about the element (*optional*).
			- **Code:** clientName
			- **Type:** Field
			- **Properties:** Mandatory
			- **Field Type:** Text
				- **Validations:** Length Greater than 0
				- **Validations:** Length Less than 30
		- **Name:** Project Name
			- **Description:** Free text description about the element (*optional*).
			- **Code**: projectName
			- **Type**: Field
			- **Properties**: Mandatory
			- **Field Type:** Text
				- **Validations**: Length Greater than 0
				- **Validations:** Length Less than 30

You'll get something like:

<div class="text--center">

![](/img/tutorial/java-APItutorial3/TUT12-04-Process.png)

</div>

### Publish the process and download the template for the Workflow Worker​

By clicking on the `Publish` button you’ll receive a confirmation request message, once you have confirmed, the process will be published.

<div class="text--center">

![](/img/tutorial/java-APItutorial3/TUT12-05-publish.png)

![](/img/tutorial/java-APItutorial3/TUT12-06-publish.png)

</div>

Now, you can download a sample Workflow Implementation from the Process Definition main page.

<div class="text--center">

![](/img/tutorial/java-APItutorial3/TUT12-07-Template_1.png)

![](/img/tutorial/java-APItutorial3/TUT12-07-Template_2.png)

</div>

This code will serve as a starting point for implementing our worker. As we'll use the Java template, the requirements for its use are the following:

- **Java JDK**
  - You need to have a Java JDK installed on your system. The current example code uses version 17, but is not required for the KuFlow SDK. You can use for example [Adoptium](https://adoptium.net/) distribution or any other. We recommend you to use a tool like [SDKMan](https://sdkman.io/jdks) to install Java SDK distributions in a comfortable way.
- **IDE**
  - An IDE with good Java support is necessary to work comfortably. You can use *VSCode*, *IntelliJ Idea,* *Eclipse* or any other with corresponding Java plugins.

### Main technologies used in the example

To make things simpler, the following technologies have been mainly used in our example:

- **Maven**
  - To build the example. It is distributed in an integrated way so it is not necessary to install it in isolation.
- **Spring Boot and Spring Cloud**
  - To wire all our integrations.
- **Temporal Java SDK**
  - To perform GRPC communications with the KuFlow temporal service.
- **KuFlow Java SDK**
  - To implement the KuFlow API Rest client, some Temporal activities and others.

## Implementation

**Note:** You can download the following project from our [public Github repository](https://github.com/kuflow/kuflow-engine-samples-java), be sure to add all the tokens and secrets from your KuFlow account and 3rd party API developers.

### Resolve dependencies​

We need to modify **pom.xml**, to include new dependencies:

```xml
    <dependency>
      <groupId>com.google.api-client</groupId>
      <artifactId>google-api-client</artifactId>
      <version>2.0.0</version>
    </dependency>
    <dependency>
      <groupId>com.google.oauth-client</groupId>
      <artifactId>google-oauth-client-jetty</artifactId>
      <version>1.34.1</version>
    </dependency>
    <dependency>
      <groupId>com.google.apis</groupId>
      <artifactId>google-api-services-script</artifactId>
      <version>v1-rev20220323-2.0.0</version>
    </dependency>
```

### Using Credentials

Now, in this step we are filling up the application configuration information. You must complete all the settings and replace the example values indicated as "FILL_ME".

#### KuFlow’s Credentials

The appropriate values can be obtained from the KuFlow application. Check out the [Create the Credentials](https://docs.kuflow.com/developers/examples/java-APItutorial3#create-the-credentials) section of this tutorial.

```yaml
# ===================================================================
# PLEASE COMPLETE ALL CONFIGURATIONS BEFORE STARTING THE WORKER
# ===================================================================

kuflow:
  api:

    # ID of the APPLICATION configured in KUFLOW.
    # Get it in "Application details" in the Kuflow APP.
    application-id: FILL_ME

    # TOKEN of the APPLICATION configured in KUFLOW.
    # Get it in "Application details" in the Kuflow APP.
    token: FILL_ME

application:
  temporal:
    # Temporal Namespace. Get it in "Application details" in the KUFLOW APP.
    namespace: FILL_ME

    # Temporal Queue. Configure it in the "Process definition" in the KUFLOW APP.
    kuflow-queue: FILL_ME

    mutual-tls:

      # Client certificate
      # Get it in "Application details" in the KUFLOW APP.
      cert-data: |
        -----BEGIN CERTIFICATE-----
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        ...
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        -----END CERTIFICATE-----


      # Private key
      # Get it in "Application details" in the KUFLOW APP.
      # IMPORTANT: This example works with PKCS8, so ensure PKCS8 is selected
      #            when you generate the certificates in the KUFLOW App
      key-data: |
        -----BEGIN PRIVATE KEY-----
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        ...
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        -----END PRIVATE KEY-----

      # KUFLOW Certification Authority (CA) of the certificates issued in KUFLOW
      ca-data: |
        -----BEGIN CERTIFICATE-----
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        ...
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_fill_me_
        -----END CERTIFICATE-----
```

Please note that this is a YAML, respect the indentation. You'll get something like this:

<div class="text--center">

![](/img/tutorial/java-APItutorial2/TUT03-07-Template_3.png)

</div>

#### 3rd Party (Google Apps Script) Credentials

We create a new file called **credentials.json** inside a subfolder **/resources**, with the content generated from the [3rd Party API (Google Apps Script) Credentials](https://docs.kuflow.com/developers/examples/java-APItutorial2#3rd-party-api-google-credentials) section on this tutorial.

<div class="text--center">

![](/img/tutorial/java-APItutorial2/TUT03-07-Template_4.png)

</div>

### Define new Activities ​

We create a new subfolder called **activity** and inside a file called **AppsScriptActivities.java** with the following content:

```java

/*
    * Copyright (c) 2023-present KuFlow S.L.
    *
    * All rights reserved.
    */
package com.kuflow.engine.samples.worker.Activity;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface AppsScriptActivities {
    String appsScriptRun(String client, String project);
}
```

We also have to create **AppsScriptActivitiesImpl.java** which is the implementation of the previous activities, using the 3rd party API methods.

In summary, the next piece of code will contain the basics methods from  [Java Quickstart](https://developers.google.com/apps-script/api/quickstart/java?hl=es-419*) modified for our use case.

- **appsScriptRun(...)**: This is our `main` method, which takes two input parameters (*Client and Project*) and a the current *Year* from the system date to create a request to execute a specific script (**SCRIPT_ID**). The request includes the name of a function (**FUNCTION_NAME**) to be executed within the script. Finally, it is printed on the console if the execution was correct or if there was an error.

**Remember** You can download the this project from our [public Github repository](https://github.com/kuflow/kuflow-engine-samples-java).

```java
/*
    * Copyright (c) 2023-present KuFlow S.L.
    *
    * All rights reserved.
    */
package com.kuflow.engine.samples.worker.Activity;

import org.springframework.stereotype.Service;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.script.Script;
import com.google.api.services.script.model.ExecutionRequest;
import com.google.api.services.script.model.Operation;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class AppsScriptActivitiesImpl implements AppsScriptActivities {
    private static final String APPLICATION_NAME = "Apps Script API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES =
    Arrays.asList("https://www.googleapis.com/auth/drive.scripts  https://www.googleapis.com/auth/script.projects https://www.googleapis.com/auth/script.processes  https://www.googleapis.com/auth/drive https://www.googleapis.com/auth/drive.scripts");
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    
    //Define some own variables
    private static final String SCRIPT_ID = "YOUR_SCRIPT_ID";
    private static final String FUNCTION_NAME = "YOUR_FUNCTION_NAME";  
    
        /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    
        private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
        throws IOException {
        // Load client secrets.
        InputStream in = AppScriptActivitiesImpl.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
        throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
    
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
    
    @Override
    public String appsScriptRun(String client, String project) {
        try{
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Script service =
            new Script.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    
        // Initialize parameters for that function.
        String year = Year.now().toString();
        List<Object> params = new ArrayList<Object>();
        params.add(client);
        params.add(project);
        params.add(year);
    
        // Create execution request.
        ExecutionRequest request = new ExecutionRequest()
                .setFunction(FUNCTION_NAME)
                .setParameters(params);
    
        Operation response = service.scripts().run(SCRIPT_ID, request).execute();
    
        // Check if the response has a result
        if (response.getResponse() != null) {
        System.out.println("Result: " + response.getResponse());
        }
    } catch (IOException | GeneralSecurityException e) {
        System.out.println("Error: " + e.getMessage());
    }
        return client;
        
    }

}

```

### Register Activities

In order to register the recently created activity in our Application worker, we must modify **TemporalBootstrap.java** as follows: 

With **//HERE** and some description before each line we'll highlight new code.

We declare the instance variable after *kuFlowActivities*:

```java

private final KuFlowActivities kuflowActivities;
//HERE Add the following line
private final AppsScriptActivities appsScriptActivities;
```

We modify the constructor as follows:

```java

    public TemporalBootstrap(
        ApplicationProperties applicationProperties,
        WorkerFactory factory,
        KuFlowSyncActivities kuFlowSyncActivities,
        KuFlowAsyncActivities kuFlowAsyncActivities,
        //HERE: Add the following line and the previous comma
        AppsScriptActivities appsScriptActivities
    ) {
        this.applicationProperties = applicationProperties;
        this.factory = factory;
        this.kuFlowSyncActivities = kuFlowSyncActivities;
        this.kuFlowAsyncActivities = kuFlowAsyncActivities;
        //HERE: Add the following line and the previous semicolon
        this.appsScriptActivities = appsScriptActivities;
    }
```

and we add the following line to register the activity in **startWorkers()**

```java

    worker.registerActivitiesImplementations(
      this.kuflowActivities,
      //HERE: Add the following line and the previous comma
      appsScriptActivities
    );
```

### Workflow Implementation

In this section, we will make the fundamental steps to creating the most basic workflow for this business process:

- Users in the organization could enter a new customer's information, such as Client Name and Project Name, as one of the steps of their typical onboarding process, and then a folder structure will be created in Google Drive corresponding to these information, executed by a Google Apps Script called by a KuFlow activity.

Open the **SampleWorkflowImpl.java** file and modify it as follows. 

With **//HERE** and some description before each line we'll highlight new code.

Declaring an instance of our new activities after kuflowActivities declaration:

```java

private final KuFlowActivities kuflowActivities;
//HERE Add the following line
private final AppsScriptActivities appsScriptActivities;
```

At the end of the constructor method **SampleWorkflowImpl()** add the following to initialize the activities:

```java
...

this.kuFlowSyncActivities = Workflow.newActivityStub(KuFlowSyncActivities.class, defaultActivityOptions);

this.kuFlowAsyncActivities = Workflow.newActivityStub(KuFlowAsyncActivities.class, asyncActivityOptions);

//HERE Add the following line
this.appsScriptActivities = Workflow.newActivityStub(AppsScriptActivities.class, defaultActivityOptions);
    }

...

```

Now we modify the **runWorkflow()** method as follows:

1. Change this line to apply the model "Task" and generate a better context. (*More info about the Task model in our [REST API reference](https://docs.kuflow.com/reference#tag/task)*).
2. Create two local variables to assign the values contained in the Data Entry Form elements.
3. Call the activity method in charge to run the script, sending the data obtained in the previous step as parameters.

```java
@Override
    public WorkflowResponse runWorkflow(WorkflowRequest request) {
        LOGGER.info("Process {} started", request.getProcessId());

        this.kuFlowGenerator = new KuFlowGenerator(request.getProcessId());
        
        //HERE 1
        Task taskDataEntry = this.createTaskDataEntry(request);

        //HERE 2
        String client = taskDataEntry.getElementValueAsString("clientName");
        String project = taskDataEntry.getElementValueAsString("projectName");

        //HERE 3
        this.appsScriptActivities.appScriptRun(client, project);

        CompleteProcessResponse completeProcess = this.completeProcess(request.getProcessId());

        LOGGER.info("Process {} finished", request.getProcessId());

        return this.completeWorkflow(completeProcess);
    }
```

The final step with the code is including the imports needed for this tutorial using some feature of your IDE (*like pressing **SHIFT+ ALT + O** in Visual Studio Code*).

## Testing

We can test all that we have done by running the worker (*like pressing **F5** in Visual Studio Code*):

<div class="text--center">

![](/img/tutorial/java-APItutorial3/TUT12-08-Test_1.png)

</div>

And initiating the process in KuFlow’s UI.

<div class="text--center">

![](/img/tutorial/java-APItutorial3/TUT12-08-Test_2.png)

</div>

Fill out the form with the information requested and complete the task.

<div class="text--center">

![](/img/tutorial/java-APItutorial3/TUT12-08-Test_3.png)

</div>

**Note:** Maybe the 3rd Party API request an authorization for access, please follow the indications in your IDE Terminal:

<div class="text--center">

![](/img/tutorial/java-APItutorial3/TUT12-08-Test_4.png)

</div>

Check the Google Drive destination folder, you'll get something like this:

<div class="text--center">

![](/img/tutorial/java-APItutorial3/TUT12-08-Test_5.png)

</div>

## Summary

In this tutorial, we have covered the basics of creating a Temporal.io-based workflow in KuFlow using a 3rd-party API (*Google Apps Script*). We have defined a new process definition, and we have built a workflow that contemplates the following business rules involving automated and human tasks:

1. Users in the organization can enter a new customer's information as part of their onboarding process.
2. The information entered includes the client name and project name.
3. A pre-defined folder structure corresponding to the entered information will be created in Google Drive.
4. The creation of the folder structure is executed by a Google Apps Script called by a KuFlow activity.

We have created a special video with the entire process:

Here you can watch all steps in the following video:

<a href="https://youtu.be/nTLGa2zheF0" target="_blank" title="Play me!">
  <p align="center">
    <img width="75%" src="https://img.youtube.com/vi/nTLGa2zheF0/maxresdefault.jpg" alt="Play me!"/>
  </p>
</a>

We sincerely hope that this step-by-step guide will help you to understand better how KuFlow can help your business to have better and more solid business processes.

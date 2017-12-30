package com.rundeck.plugin.util

import com.dtolabs.rundeck.core.execution.workflow.steps.StepException
import com.dtolabs.rundeck.plugins.step.PluginStepContext
import com.rundeck.plugin.MesosFailReason
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

/**
 * Created by carlos on 30/12/17.
 */
class RestClientUtils {
    public static final String URI_PATH = "/v2/apps/"
    public static final String URI_TASKS = '/tasks'

    public static putApp(String mesosServiceApiURL, String appId, Map properties, PluginStepContext context){
        def serviceAPI = new HTTPBuilder(mesosServiceApiURL)
        serviceAPI.request(Method.PUT, ContentType.JSON){ req ->
            uri.path = URI_PATH + appId
            uri.query = [format:'json']
            body = properties

            response.success = { resp, json ->
                assert [200, 201].contains(resp.status)
                HashMap<String, String> meta = new HashMap<>();
                meta.put("content-data-type", "application/json");
                context.getExecutionContext().getExecutionListener().event("log", json.toString(), meta);
            }
            response.'400' = { resp ->
                throw new StepException(
                        "Invalid JSON",
                        e,
                        MesosFailReason.InvalidJSON
                );
            }
            response.'401' = { resp ->
                throw new StepException(
                        "Invalid username or password.",
                        e,
                        MesosFailReason.InvalidUser
                );
            }
            response.'403' = { resp ->
                throw new StepException(
                        "Not Authorized to perform this action!",
                        e,
                        MesosFailReason.NotAuthorized
                );
            }
            response.'409' = { resp ->
                throw new StepException(
                        "An app with id [/existing_app] already exists.",
                        e,
                        MesosFailReason.AppAlreadyExists
                );
            }
            response.'422' = { resp ->
                throw new StepException(
                        "Object is not valid",
                        e,
                        MesosFailReason.InvalidObject
                );
            }
            response.failure = { resp, json ->
                throw new StepException(
                        "Put app on mesos service error",
                        e,
                        MesosFailReason.requestFailed
                );
            }
        }
    }

    public static getTaskApp(String mesosServiceApiURL, String appId, PluginStepContext context){
        def serviceAPI = new HTTPBuilder(mesosServiceApiURL)
        serviceAPI.request(Method.GET, ContentType.JSON){ req ->
            uri.path = URI_PATH + appId + URI_TASKS
            uri.query = [:]

            response.success = { resp, json ->
                assert [200].contains(resp.status)
                HashMap<String, String> meta = new HashMap<>();
                meta.put("content-data-type", "application/json");
                context.getExecutionContext().getExecutionListener().event("log", json.toString(), meta);
            }
            response.'401' = { resp ->
                throw new StepException(
                        "Invalid username or password.",
                        e,
                        MesosFailReason.InvalidUser
                );
            }
            response.'403' = { resp ->
                throw new StepException(
                        "Not Authorized to perform this action!",
                        e,
                        MesosFailReason.NotAuthorized
                );
            }
            response.'404' = { resp ->
                throw new StepException(
                        "App '/not_existent' does not exist",
                        e,
                        MesosFailReason.AppNotExists
                );
            }
            response.failure = { resp, json ->
                throw new StepException(
                        "Put app on mesos service error",
                        e,
                        MesosFailReason.requestFailed
                );
            }
        }
    }

    public static deleteTaskApp(String mesosServiceApiURL, String appId, Map queryParams, PluginStepContext context){
        def serviceAPI = new HTTPBuilder(mesosServiceApiURL)
        serviceAPI.request(Method.DELETE, ContentType.JSON){ req ->
            uri.path = URI_PATH + appId + URI_TASKS
            uri.query = queryParams.findAll {it.value}

            response.success = { resp, json ->
                assert [200].contains(resp.status)
                HashMap<String, String> meta = new HashMap<>();
                meta.put("content-data-type", "application/json");
                context.getExecutionContext().getExecutionListener().event("log", json.toString(), meta);
            }
            response.'401' = { resp ->
                throw new StepException(
                        "Invalid username or password.",
                        e,
                        MesosFailReason.InvalidUser
                );
            }
            response.'403' = { resp ->
                throw new StepException(
                        "Not Authorized to perform this action!",
                        e,
                        MesosFailReason.NotAuthorized
                );
            }
            response.'404' = { resp ->
                throw new StepException(
                        "App '/not_existent' does not exist",
                        e,
                        MesosFailReason.AppNotExists
                );
            }
            response.'409' = { resp ->
                throw new StepException(
                        "App is locked by one or more deployments. Override with the option '?force=true'. View details at '/v2/deployments/<DEPLOYMENT_ID>'.",
                        e,
                        MesosFailReason.AppAlreadyExists
                );
            }
            response.failure = { resp, json ->
                throw new StepException(
                        "Put app on mesos service error",
                        e,
                        MesosFailReason.requestFailed
                );
            }
        }
    }
}

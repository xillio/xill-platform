package nl.xillio.xill.webservice.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller to redirect all requests to the service base url to the swagger UI.
 */
@Controller
public class SwaggerController {
    @RequestMapping(path = "${xws.api.base.path}", method = RequestMethod.GET)
    public String apiDocumentation() {
        return "redirect:/swagger-ui.html";
    }

}

package com.hp.example.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.RequestToViewNameTranslator;
import org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@Component("viewNameTranslator")
public final class MediaTypeRequestToViewTranslator implements RequestToViewNameTranslator {
    @Autowired
    private ContentNegotiationManager contentNegotiationManager;

    @SuppressWarnings("UnusedDeclaration")
    private DefaultRequestToViewNameTranslator defaultTranslator = new DefaultRequestToViewNameTranslator();

    // a list of media types to ignore - not output on the translated view
    private List<String> ignoredTypes = Arrays.asList("text/html");

    @Override
    public String getViewName(HttpServletRequest request) {
        // first resolve the media type (see below)
        String mediaType = resolveMediaType(request);

        // delegate to the default translator to get the view name
        String viewName = defaultTranslator.getViewName(request);

        // concatenate the resolved media type to the default name and return it
        return viewName + mediaType;
    }

    private String resolveMediaType(HttpServletRequest request) {
        try {
            // use the content negotiation manager to resolve the media type from the request. The manager does it
            // according to its own search path and preferences - using the suffix, using the Accept header and
            // preferring one of them. The result would always be a single type or none at all, but it returns a list
            List<MediaType> types = contentNegotiationManager.resolveMediaTypes(new ServletWebRequest(request));

            // resolve to a single type
            String type = types == null || types.size()==0 ? "" : types.get(0).toString();

            // if it's not in the ignored media types - prepend a semi-colon and return it
            return ignoredTypes.contains(type) ? "" : ";" + type;
        } catch (HttpMediaTypeNotAcceptableException e) {
            return "";
        }
    }
}

package com.github.mdeluise.ytsms.subscription.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mdeluise.ytsms.TestEnvironment;
import com.github.mdeluise.ytsms.authentication.User;
import com.github.mdeluise.ytsms.authentication.UserService;
import com.github.mdeluise.ytsms.channel.Channel;
import com.github.mdeluise.ytsms.exception.ResourceNotFoundException;
import com.github.mdeluise.ytsms.security.apikey.ApiKeyFilter;
import com.github.mdeluise.ytsms.security.apikey.ApiKeyRepository;
import com.github.mdeluise.ytsms.security.apikey.ApiKeyService;
import com.github.mdeluise.ytsms.security.jwt.JwtTokenFilter;
import com.github.mdeluise.ytsms.security.jwt.JwtTokenUtil;
import com.github.mdeluise.ytsms.security.jwt.JwtWebUtil;
import com.github.mdeluise.ytsms.subscription.Subscription;
import com.github.mdeluise.ytsms.subscription.SubscriptionController;
import com.github.mdeluise.ytsms.subscription.SubscriptionDTO;
import com.github.mdeluise.ytsms.subscription.SubscriptionDTOConverter;
import com.github.mdeluise.ytsms.subscription.SubscriptionService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

@WebMvcTest(SubscriptionController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "user")
@Import(TestEnvironment.class)
public class SubscriptionControllerTest {
    @MockBean
    JwtTokenFilter jwtTokenFilter;
    @MockBean
    JwtTokenUtil jwtTokenUtil;
    @MockBean
    JwtWebUtil jwtWebUtil;
    @MockBean
    ApiKeyFilter apiKeyFilter;
    @MockBean
    ApiKeyService apiKeyService;
    @MockBean
    ApiKeyRepository apiKeyRepository;
    @MockBean
    SubscriptionService subscriptionService;
    @MockBean
    SubscriptionDTOConverter subscriptionDTOConverter;
    @MockBean
    UserService userService;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;


    @Test
    void whenGetSubscriptions_ShouldReturnSubscriptions() throws Exception {
        Subscription subscription1 = new Subscription();
        subscription1.setId(1L);
        subscription1.setChannel(new Channel());
        SubscriptionDTO subscriptionDTO1 = new SubscriptionDTO();
        subscriptionDTO1.setId(1L);
        subscriptionDTO1.setChannelId("channelId1");
        Subscription subscription2 = new Subscription();
        subscription2.setId(2L);
        subscription2.setChannel(new Channel());
        SubscriptionDTO subscriptionDTO2 = new SubscriptionDTO();
        subscriptionDTO2.setId(2L);
        subscriptionDTO2.setChannelId("channelId1");
        Mockito.when(subscriptionService.getAll()).thenReturn(List.of(subscription1, subscription2));
        Mockito.when(subscriptionDTOConverter.convertToDTO(subscription1)).thenReturn(subscriptionDTO1);
        Mockito.when(subscriptionDTOConverter.convertToDTO(subscription2)).thenReturn(subscriptionDTO2);

        mockMvc.perform(MockMvcRequestBuilders.get("/subscription")).andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
               .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)));
    }


    @Test
    void whenDeleteSubscription_shouldReturnOk() throws Exception {
        Mockito.doNothing().when(subscriptionService).remove(0L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/subscription/0"))
               .andExpect(MockMvcResultMatchers.status().isOk());
    }


    @Test
    void whenDeleteNonExistingSubscription_shouldError() throws Exception {
        Mockito.doThrow(ResourceNotFoundException.class).when(subscriptionService).remove(0L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/subscription/0"))
               .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }


    @Test
    void whenCreateSubscription_shouldReturnSubscription() throws Exception {
        User user = new User();
        user.setId(0L);
        Subscription created = new Subscription();
        created.setId(0L);
        created.setChannel(new Channel());
        created.setUser(user);
        SubscriptionDTO createdDTO = new SubscriptionDTO();
        createdDTO.setId(0L);
        createdDTO.setChannelId("channelId1");
        createdDTO.setUserId(0L);
        Mockito.when(subscriptionService.save(created)).thenReturn(created);
        Mockito.when(subscriptionDTOConverter.convertToDTO(created)).thenReturn(createdDTO);
        Mockito.when(subscriptionDTOConverter.convertFromDTO(createdDTO)).thenReturn(created);
        Mockito.when(userService.get("user")).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.post("/subscription").content(
                                                  objectMapper.writeValueAsString(subscriptionDTOConverter.convertToDTO(created)))
                                              .contentType(MediaType.APPLICATION_JSON))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$.channelId").value("channelId1"))
               .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(0));
    }

}

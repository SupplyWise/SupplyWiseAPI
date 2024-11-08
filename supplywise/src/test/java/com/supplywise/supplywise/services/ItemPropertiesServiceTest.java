// package com.supplywise.supplywise.services;

// import com.supplywise.supplywise.model.ItemProperties;
// import com.supplywise.supplywise.repositories.ItemPropertiesRepository;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;

// import java.util.Optional;
// import java.util.UUID;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.*;

// class ItemPropertiesServiceTest {

//     @Mock
//     private ItemPropertiesRepository itemPropertiesRepository;

//     @InjectMocks
//     private ItemPropertiesService itemPropertiesService;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//     }

//     @Test
//     void testCreateItemProperties_ShouldSaveItemProperties() {
//         // ItemProperties data
//         ItemProperties itemProperties = new ItemProperties();
//         itemProperties.setProperty("Test Property");

//         // Mock the repository to return the itemProperties when saved
//         when(itemPropertiesRepository.save(any(ItemProperties.class))).thenReturn(itemProperties);

//         // Execute the method
//         ItemProperties createdItemProperties = itemPropertiesService.createItemProperties(itemProperties);

//         // Verify that the itemProperties is saved
//         verify(itemPropertiesRepository, times(1)).save(itemProperties);

//         // Check if the itemProperties returned matches the mock
//         assertEquals("Test Property", createdItemProperties.getProperty());
//     }

//     @Test
//     void testGetItemPropertiesById_ItemFound_ShouldReturnItemProperties() {
//         // Generate a random UUID for the itemProperties
//         UUID itemPropertiesId = UUID.randomUUID();
//         ItemProperties itemProperties = new ItemProperties();
//         itemProperties.setId(itemPropertiesId);
//         itemProperties.setProperty("Test Property");

//         // Mock the repository to return the itemProperties when searched by ID
//         when(itemPropertiesRepository.findById(itemPropertiesId)).thenReturn(Optional.of(itemProperties));

//         // Execute the method
//         Optional<ItemProperties> foundItemProperties = itemPropertiesService.getItemPropertiesById(itemPropertiesId);

//         // Check if the itemProperties is returned
//         assertTrue(foundItemProperties.isPresent());
//         assertEquals(itemPropertiesId, foundItemProperties.get().getId());
//         assertEquals("Test Property", foundItemProperties.get().getProperty());

//         // Verify that the findById method was called
//         verify(itemPropertiesRepository, times(1)).findById(itemPropertiesId);
//     }

//     @Test
//     void testGetItemPropertiesById_ItemNotFound_ShouldReturnEmpty() {
//         // Generate a random UUID for the itemProperties
//         UUID itemPropertiesId = UUID.randomUUID();

//         // Mock the repository to return an empty result when searching by ID
//         when(itemPropertiesRepository.findById(itemPropertiesId)).thenReturn(Optional.empty());

//         // Execute the method
//         Optional<ItemProperties> foundItemProperties = itemPropertiesService.getItemPropertiesById(itemPropertiesId);

//         // Check if no itemProperties is found
//         assertFalse(foundItemProperties.isPresent());

//         // Verify that the findById method was called
//         verify(itemPropertiesRepository, times(1)).findById(itemPropertiesId);
//     }

//     @Test
//     void testDeleteItemProperties_ShouldDeleteItemProperties() {
//         // Generate a random UUID for the itemProperties
//         UUID itemPropertiesId = UUID.randomUUID();

//         // Mock the repository to do nothing when deleting
//         doNothing().when(itemPropertiesRepository).deleteById(itemPropertiesId);

//         // Execute the method
//         itemPropertiesService.deleteItemProperties(itemPropertiesId);

//         // Verify that the deleteById method was called
//         verify(itemPropertiesRepository, times(1)).deleteById(itemPropertiesId);
//     }
// }

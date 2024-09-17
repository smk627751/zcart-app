# zcart

zcart is an e-commerce Android application that allows users to browse products, add them to their cart, and place orders. The app features Firebase integration for product and user data management, and it adheres to Google's Material Design guidelines for a smooth and visually appealing user experience.

## Features

### 1. **Product Listings**
   - Browse a variety of products across different categories like Electronics, Fashion, Home & Kitchen, and more.
   - Products are displayed using a `StaggeredGridLayout` for an aesthetic and responsive design.
   - Categories include sub-categories for a more specific search.

### 2. **Search and Filter**
   - Search for products by name or category using a **Material Search Bar**.
   - The app supports dynamic search and filters, allowing users to quickly find the products they need.

### 3. **Cart Management**
   - Add products to the cart with a single tap.
   - Products in the cart are dynamically updated, and users can adjust quantities or remove items.
   - After adding items, users are redirected to the cart for easy checkout.

### 4. **Order Management**
   - View and track orders directly from the app.
   - Manage active and completed orders with an intuitive interface.

### 5. **Firebase Integration**
   - Real-time product data synchronization using **Firestore**.
   - Notification system that informs users of order updates, reviews, and other events.

### 6. **Notifications**
   - Receive real-time notifications for new orders and reviews.
   - Notifications are categorized into different types like `OrderNotification` and `ReviewNotification`.
   - Each type of notification is handled using a dedicated class and displayed in the app.

### 7. **Shared Element Transitions**
   - Smooth transitions between product listing and detailed product views using **MaterialContainerTransform**.
   - Shared element transitions for a more immersive user experience when navigating between activities.

### 8. **Material Design**
   - The app follows **Material Design 3 (M3)** guidelines for a modern look and feel.
   - Includes **CollapsingToolbarLayout**, **BottomNavigationView**, and **MaterialToolbar** for easy navigation.
   - Uses **ShimmerFrameLayout** for placeholder loading screens, optimized for performance on lower-end devices.

### 9. **Responsive Design**
   - Supports both portrait and landscape orientations, with dynamic adjustments to the layout based on screen size and orientation.
   - On landscape, uses a **NavigationRailView** to enhance navigation for larger screens.

### 10. **User Authentication**
   - User login and registration via Firebase Authentication.
   - Allows users to sign in, view their order history, and manage their profiles.

### 11. **Product Management (Vendor-Specific)**
   - Vendor users can add or edit products, including uploading images, selecting categories, and managing product details.
   - Supports image cropping using **UCrop** for product images.

### 12. **Lazy Loading**
   - Products are loaded dynamically with lazy loading techniques, ensuring a fast and efficient user experience even with large datasets.

### 13. **Category and Subcategory Management**
   - Users can browse through detailed categories and subcategories for better product discovery.
   - Categories are dynamically updated from the database.

## Tech Stack

- **Kotlin**: The primary programming language used for Android development.
- **Firebase Firestore**: For real-time data storage and synchronization.
- **Firebase Authentication**: For managing user authentication and login.
- **Glide**: For efficient image loading and caching.
- **Material Design**: For creating a clean and intuitive UI.
- **UCrop**: For image cropping functionality in product management.
- **Shimmer**: For loading placeholder animations.
- **RecyclerView**: For displaying lists of products, orders, and notifications.

## How to Run

1. Clone this repository:
   ```bash
   git clone https://github.com/smk627751/zcart.git

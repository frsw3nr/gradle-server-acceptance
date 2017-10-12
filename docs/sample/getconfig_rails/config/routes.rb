Rails.application.routes.draw do
  get 'platforms/import'

  # For details on the DSL available within this file, see http://guides.rubyonrails.org/routing.html
  resources :tenants
  resources :accounts
  resources :platforms
end

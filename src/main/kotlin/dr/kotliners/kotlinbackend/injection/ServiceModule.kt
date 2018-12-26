package dr.kotliners.kotlinbackend.injection

import dagger.Module
import dagger.Provides
import dr.kotliners.kotlinbackend.internal.InternalService
import dr.kotliners.kotlinbackend.internal.InternalServiceImpl

@Module
class ServiceModule {
    @Provides
    fun provideInternalService(serviceImpl: InternalServiceImpl): InternalService {
        return serviceImpl
    }
}